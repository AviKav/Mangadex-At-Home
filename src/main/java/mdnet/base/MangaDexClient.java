package mdnet.base;

import mdnet.base.settings.ClientSettings;
import mdnet.base.server.ApplicationKt;
import mdnet.base.server.WebUiKt;
import mdnet.base.settings.ServerSettings;
import mdnet.cache.DiskLruCache;
import org.http4k.server.Http4kServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static mdnet.base.Constants.JACKSON;

public class MangaDexClient {
	private final static Logger LOGGER = LoggerFactory.getLogger(MangaDexClient.class);

	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private final ServerHandler serverHandler;
	private final ClientSettings clientSettings;

	private final Map<Instant, Statistics> statsMap = Collections
			.synchronizedMap(new LinkedHashMap<Instant, Statistics>(240) {
				@Override
				protected boolean removeEldestEntry(Map.Entry eldest) {
					return this.size() > 240;
				}
			});
	private final AtomicReference<Statistics> statistics;
	private final AtomicBoolean isHandled;

	private ServerSettings serverSettings;
	private Http4kServer engine; // if this is null, then the server has shutdown
	private Http4kServer webUi;
	private DiskLruCache cache;

	// these variables are for runLoop();
	private int counter = 0;
	private long lastBytesSent = 0;
	// a non-negative number here means we are shutting down
	private int gracefulCounter = -1;
	private Runnable gracefulAction;

	public MangaDexClient(ClientSettings clientSettings) {
		this.clientSettings = clientSettings;
		this.serverHandler = new ServerHandler(clientSettings);
		this.statistics = new AtomicReference<>();
		this.isHandled = new AtomicBoolean();

		try {
			cache = DiskLruCache.open(new File("cache"), 1, 1,
					clientSettings.getMaxCacheSizeInMebibytes() * 1024 * 1024 /* MiB to bytes */);

			DiskLruCache.Snapshot snapshot = cache.get("statistics");
			if (snapshot != null) {
				statistics.set(JACKSON.readValue(snapshot.getInputStream(0), Statistics.class));
				snapshot.close();
			} else {
				statistics.set(new Statistics());
			}
		} catch (IOException e) {
			Main.dieWithError(e);
		}
	}

	public void runLoop() {
		loginAndStartServer();
		if (serverSettings.getLatestBuild() > Constants.CLIENT_BUILD) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Outdated build detected! Latest: {}, Current: {}", serverSettings.getLatestBuild(),
						Constants.CLIENT_BUILD);
			}
		}

		lastBytesSent = statistics.get().getBytesSent();
		statsMap.put(Instant.now(), statistics.get());

		if (clientSettings.getWebSettings() != null) {
			webUi = WebUiKt.getUiServer(clientSettings.getWebSettings(), statistics, statsMap);
			webUi.start();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Mangadex@Home Client initialization completed successfully. Starting normal operation.");
		}

		executorService.scheduleWithFixedDelay(() -> {
			try {
				// Converting from 15 seconds loop to 45 second loop
				if (counter / 3 == 80) {
					counter = 0;
					lastBytesSent = statistics.get().getBytesSent();

					if (engine == null) {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Restarting server stopped due to hourly bandwidth limit");
						}

						loginAndStartServer();
					}
				} else {
					counter++;
				}

				if (gracefulCounter == 0) {
					logout();
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Waiting another 15 seconds for graceful shutdown ({} out of {} tries)",
								gracefulCounter + 1, 4);
					}
					gracefulCounter++;
				} else if (gracefulCounter > 0) {
					if (!isHandled.get() || gracefulCounter == 4 || engine == null) {
						if (LOGGER.isInfoEnabled()) {
							if (!isHandled.get()) {
								LOGGER.info("No requests received, shutting down");
							} else {
								LOGGER.info("Max tries attempted, shutting down");
							}
						}

						if (engine != null) {
							stopServer();
						}
						if (gracefulAction != null) {
							gracefulAction.run();
						}

						// reset variables
						gracefulCounter = -1;
						gracefulAction = null;
					} else {
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Waiting another 15 seconds for graceful shutdown ({} out of {} tries)",
									gracefulCounter + 1, 4);
						}
						gracefulCounter++;
					}
					isHandled.set(false);
				} else {
					if (counter % 3 == 0) {
						pingControl();
					}
					updateStats();
				}

			} catch (Exception e) {
				LOGGER.warn("statistics update failed", e);
			}

		}, 15, 15, TimeUnit.SECONDS);
	}

	private void pingControl() {
		// if the server is offline then don't try and refresh certs
		if (engine == null) {
			return;
		}

		long currentBytesSent = statistics.get().getBytesSent() - lastBytesSent;
		if (clientSettings.getMaxMebibytesPerHour() != 0
				&& clientSettings.getMaxMebibytesPerHour() * 1024 * 1024 /* MiB to bytes */ < currentBytesSent) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Shutting down server as hourly bandwidth limit reached");
			}

			// Give enough time for graceful shutdown
			if (240 - counter > 3) {
				LOGGER.info("Graceful shutdown started");
				gracefulCounter = 0;
			}
		}

		ServerSettings n = serverHandler.pingControl(serverSettings);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Server settings received: {}", n);
		}

		if (n != null) {
			if (n.getLatestBuild() > Constants.CLIENT_BUILD) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Outdated build detected! Latest: {}, Current: {}", n.getLatestBuild(),
							Constants.CLIENT_BUILD);
				}
			}

			if (n.getTls() != null || !n.getImageServer().equals(serverSettings.getImageServer())) {
				// certificates or upstream url must have changed, restart webserver
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Doing internal restart of HTTP server to refresh certs/upstream URL");
				}

				LOGGER.info("Graceful shutdown started");
				gracefulCounter = 0;
				gracefulAction = this::loginAndStartServer;
			}
		}
	}

	private void updateStats() throws IOException {
		statistics.updateAndGet(n -> n.copy(n.getRequestsServed(), n.getCacheHits(), n.getCacheMisses(),
				n.getBrowserCached(), n.getBytesSent(), cache.size()));

		statsMap.put(Instant.now(), statistics.get());

		DiskLruCache.Editor editor = cache.edit("statistics");
		if (editor != null) {
			JACKSON.writeValue(editor.newOutputStream(0), Statistics.class);
			editor.commit();
		}
	}

	private void loginAndStartServer() {
		serverSettings = serverHandler.loginToControl();
		if (serverSettings == null) {
			Main.dieWithError("Failed to get a login response from server - check API secret for validity");
		}
		engine = ApplicationKt.getServer(cache, serverSettings, clientSettings, statistics, isHandled);
		engine.start();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Internal HTTP server was successfully started");
		}
	}

	private void logout() {
		serverHandler.logoutFromControl();
	}

	private void stopServer() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Shutting down HTTP server");
		}
		engine.stop();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Internal HTTP server has gracefully shut down");
		}
		engine = null;
	}

	public void shutdown() {
		LOGGER.info("Graceful shutdown started");
		gracefulCounter = 0;
		AtomicBoolean readyToExit = new AtomicBoolean(false);
		gracefulAction = () -> {
			if (webUi != null) {
				webUi.close();
			}
			try {
				cache.close();
			} catch (IOException e) {
				LOGGER.error("Cache failed to close", e);
			}
			executorService.shutdown();
			readyToExit.set(true);
		};
		while (!readyToExit.get()) {
		}
	}
}
