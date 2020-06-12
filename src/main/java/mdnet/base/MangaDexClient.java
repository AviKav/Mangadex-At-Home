package mdnet.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mdnet.base.settings.ClientSettings;
import mdnet.base.web.ApplicationKt;
import mdnet.base.web.WebUiKt;
import mdnet.cache.DiskLruCache;
import mdnet.webui.WebConsole;
import org.http4k.server.Http4kServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MangaDexClient {
	private final static Logger LOGGER = LoggerFactory.getLogger(MangaDexClient.class);

	// This lock protects the Http4kServer from concurrent restart attempts
	private final Object shutdownLock = new Object();
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private final ServerHandler serverHandler;
	private final ClientSettings clientSettings;
	private final AtomicReference<Statistics> statistics;
	private ServerSettings serverSettings;

	// if this is null, then the server has shutdown
	private Http4kServer engine;
	private Http4kServer webUi;
	private DiskLruCache cache;

	public MangaDexClient(ClientSettings clientSettings) {
		this.clientSettings = clientSettings;
		this.serverHandler = new ServerHandler(clientSettings);
		this.statistics = new AtomicReference<>();

		try {
			cache = DiskLruCache.open(new File("cache"), 3, 3,
					clientSettings.getMaxCacheSizeMib() * 1024 * 1024 /* MiB to bytes */);
		} catch (IOException e) {
			MangaDexClient.dieWithError(e);
		}
	}

	// This function also does most of the program initialization.
	public void runLoop() {
		statistics.set(new Statistics(0));
		loginAndStartServer();
		if (serverSettings.getLatestBuild() > Constants.CLIENT_BUILD) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Outdated build detected! Latest: {}, Current: {}", serverSettings.getLatestBuild(),
						Constants.CLIENT_BUILD);
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("MDNet initialization completed successfully. Starting normal operation.");
		}

		webUi = WebUiKt.getUiServer(clientSettings.getWebSettings(), statistics);
		webUi.start();

		// we don't really care about the Atomic part here
		AtomicInteger counter = new AtomicInteger();
		// ping keep-alive every 45 seconds
		executorService.scheduleAtFixedRate(() -> {
			int num = counter.get();
			if (num == 80) {
				counter.set(0);

				// if server is stopped due to egress limits, restart it
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Hourly update: refreshing statistics");
				}
				statistics.set(new Statistics(statistics.get().getSequenceNumber() + 1));

				if (engine == null) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Restarting server stopped due to hourly bandwidth limit");
					}

					loginAndStartServer();
				}
			} else {
				counter.set(num + 1);
			}

			// if the server is offline then don't try and refresh certs
			if (engine == null) {
				return;
			}

			if (clientSettings.getMaxBandwidthMibPerHour() != 0 && clientSettings.getMaxBandwidthMibPerHour() * 1024
					* 1024 /* MiB to bytes */ < statistics.get().getBytesSent().get()) {
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

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		ClientSettings settings;

		try {
			settings = gson.fromJson(new FileReader(file), ClientSettings.class);
		} catch (FileNotFoundException ignored) {
			settings = new ClientSettings();
			LOGGER.warn("Settings file {} not found, generating file", file);
			try (FileWriter writer = new FileWriter(file)) {
				writer.write(gson.toJson(settings));
			} catch (IOException e) {
				MangaDexClient.dieWithError(e);
			}
		}

		if (!ClientSettings.isSecretValid(settings.getClientSecret()))
			MangaDexClient.dieWithError("Config Error: API Secret is invalid, must be 52 alphanumeric characters");

		if (settings.getClientPort() == 0) {
			MangaDexClient.dieWithError("Config Error: Invalid port number");
		}

		if (settings.getMaxCacheSizeMib() < 1024) {
			MangaDexClient.dieWithError("Config Error: Invalid max cache size, must be >= 1024 MiB (1GiB)");
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Client settings loaded: {}", settings);
		}

		MangaDexClient client = new MangaDexClient(settings);
		Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));
		client.runLoop();

		if (settings.getWebSettings() != null) {
			// java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
			// System.setOut(new java.io.PrintStream(out));
			// TODO: system.out redirect
			ClientSettings finalSettings = settings;
			new Thread(() -> {
				WebConsole webConsole = new WebConsole(finalSettings.getWebSettings().getUiWebsocketPort()) {
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
	}

	public static void dieWithError(Throwable e) {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error("Critical Error", e);
		}
		System.exit(1);
	}

	public static void dieWithError(String error) {
		if (LOGGER.isErrorEnabled()) {
			LOGGER.error("Critical Error: " + error);
		}
		System.exit(1);
	}
}
