package mdnet.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import mdnet.base.settings.ClientSettings;
import mdnet.base.settings.WebSettings;
import mdnet.base.web.ApplicationKt;
import mdnet.base.web.WebUiKt;
import mdnet.cache.DiskLruCache;
import mdnet.webui.WebConsole;
import org.http4k.server.Http4kServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class MangaDexClient {
	private final static Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private final static Logger LOGGER = LoggerFactory.getLogger(MangaDexClient.class);

	// This lock protects the Http4kServer from concurrent restart attempts
	private final Object shutdownLock = new Object();
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private final ServerHandler serverHandler;
	private final ClientSettings clientSettings;
	private final AtomicReference<Statistics> statistics;

	private ServerSettings serverSettings;
	private Http4kServer engine; // if this is null, then the server has shutdown
	private Http4kServer webUi;
	private DiskLruCache cache;

	// these variables are for runLoop();
	private int counter = 0;
	private long lastBytesSent = 0;

	public MangaDexClient(ClientSettings clientSettings) {
		this.clientSettings = clientSettings;
		this.serverHandler = new ServerHandler(clientSettings);
		this.statistics = new AtomicReference<>();

		try {
			cache = DiskLruCache.open(new File("cache"), 3, 3,
					clientSettings.getMaxCacheSizeMib() * 1024 * 1024 /* MiB to bytes */);

			DiskLruCache.Snapshot snapshot = cache.get("statistics");
			if (snapshot != null) {
				String json = snapshot.getString(0);
				snapshot.close();
				statistics.set(GSON.fromJson(json, new TypeToken<ArrayList<Statistics>>() {
				}.getType()));
			} else {
				statistics.set(new Statistics());
			}
			lastBytesSent = statistics.get().getBytesSent();
		} catch (IOException e) {
			MangaDexClient.dieWithError(e);
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

		if (clientSettings.getWebSettings() != null) {
			webUi = WebUiKt.getUiServer(clientSettings.getWebSettings(), statistics);
			webUi.start();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("MDNet initialization completed successfully. Starting normal operation.");
		}

		executorService.scheduleAtFixedRate(() -> {
			if (counter == 80) {
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

			// if the server is offline then don't try and refresh certs
			if (engine == null) {
				return;
			}

			long currentBytesSent = statistics.get().getBytesSent() - lastBytesSent;
			if (clientSettings.getMaxBandwidthMibPerHour() != 0
					&& clientSettings.getMaxBandwidthMibPerHour() * 1024 * 1024 /* MiB to bytes */ < currentBytesSent) {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Shutting down server as hourly bandwidth limit reached");
				}

				synchronized (shutdownLock) {
					logoutAndStopServer();
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

					synchronized (shutdownLock) {
						logoutAndStopServer();
						loginAndStartServer();
					}
				}
			}
		}, 45, 45, TimeUnit.SECONDS);

	}

	private void loginAndStartServer() {
		serverSettings = serverHandler.loginToControl();
		if (serverSettings == null) {
			MangaDexClient.dieWithError("Failed to get a login response from server - check API secret for validity");
		}
		engine = ApplicationKt.getServer(cache, serverSettings, clientSettings, statistics);
		engine.start();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Internal HTTP server was successfully started");
		}
	}

	private void logoutAndStopServer() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Gracefully shutting down HTTP server");
		}
		serverHandler.logoutFromControl();
		engine.stop();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Internal HTTP server has gracefully shut down");
		}
		engine = null;
	}

	public void shutdown() {
		executorService.shutdown();
		synchronized (shutdownLock) {
			if (engine == null) {
				return;
			}

			logoutAndStopServer();
		}
		webUi.close();
		try {
			cache.close();
		} catch (IOException e) {
			LOGGER.error("Cache failed to close", e);
		}
	}

	public static void main(String[] args) {
		System.out.println("Mangadex@Home Client " + Constants.CLIENT_VERSION + " (Build " + Constants.CLIENT_BUILD
				+ ") initializing\n");
		System.out.println("Copyright (c) 2020, MangaDex Network");

		String file = "settings.json";
		if (args.length == 1) {
			file = args[0];
		} else if (args.length != 0) {
			MangaDexClient.dieWithError("Expected one argument: path to config file, or nothing");
		}

		ClientSettings settings;

		try {
			settings = GSON.fromJson(new FileReader(file), ClientSettings.class);
		} catch (FileNotFoundException ignored) {
			settings = new ClientSettings();
			LOGGER.warn("Settings file {} not found, generating file", file);
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(GSON.toJson(settings));
			} catch (IOException e) {
				MangaDexClient.dieWithError(e);
			}
		}

		validateSettings(settings);

		if (settings.getWebSettings() != null) {
			WebSettings webSettings = settings.getWebSettings();

			// TODO: system.out redirect
			new Thread(() -> {
				WebConsole webConsole = new WebConsole(webSettings.getUiWebsocketPort()) {
					@Override
					protected void parseMessage(String message) {
						System.out.println(message);
						// TODO: something happens here
						// the message should be formatted in json
					}
				};
				// TODO: webConsole.sendMessage(t,m) whenever system.out is written to
			}).start();
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Client settings loaded: {}", settings);
		}

		MangaDexClient client = new MangaDexClient(settings);
		Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));
		client.runLoop();
	}

	public static void dieWithError(Throwable e) {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error("Critical Error", e);
		}
		System.exit(1);
	}

	public static void dieWithError(String error) {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error("Critical Error: {}", error);
		}
		System.exit(1);
	}

	public static void validateSettings(ClientSettings settings) {
		if (!isSecretValid(settings.getClientSecret()))
			MangaDexClient.dieWithError("Config Error: API Secret is invalid, must be 52 alphanumeric characters");

		if (settings.getClientPort() == 0) {
			MangaDexClient.dieWithError("Config Error: Invalid port number");
		}

		if (settings.getMaxCacheSizeMib() < 1024) {
			MangaDexClient.dieWithError("Config Error: Invalid max cache size, must be >= 1024 MiB (1GiB)");
		}

		if (settings.getThreads() < 4) {
			MangaDexClient.dieWithError("Config Error: Invalid number of threads, must be >= 8");
		}

		if (settings.getMaxBandwidthMibPerHour() < 0) {
			MangaDexClient.dieWithError("Config Error: Max bandwidth must be >= 0");
		}

		if (settings.getMaxBurstRateKibPerSecond() < 0) {
			MangaDexClient.dieWithError("Config Error: Max burst rate must be >= 0");
		}

		if (settings.getWebSettings() != null) {
			if (settings.getWebSettings().getUiPort() == 0) {
				MangaDexClient.dieWithError("Config Error: Invalid UI port number");
			}

			if (settings.getWebSettings().getUiWebsocketPort() == 0) {
				MangaDexClient.dieWithError("Config Error: Invalid websocket port number");
			}
		}
	}

	public static boolean isSecretValid(String clientSecret) {
		final int CLIENT_KEY_LENGTH = 52;
		return Pattern.matches("^[a-zA-Z0-9]{" + CLIENT_KEY_LENGTH + "}$", clientSecret);
	}
}
