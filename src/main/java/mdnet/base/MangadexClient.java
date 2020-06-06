package mdnet.base;

import com.google.gson.Gson;
import mdnet.cache.DiskLruCache;
import org.http4k.server.Http4kServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MangadexClient {
	private final static Logger LOGGER = LoggerFactory.getLogger(MangadexClient.class);

	// This lock protects the Http4kServer from concurrent restart attempts
	private final Object shutdownLock = new Object();
	private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
	private final ServerHandler serverHandler;
	private final ClientSettings clientSettings;
	private final AtomicReference<Statistics> statistics;
	private ServerSettings serverSettings;

	// if this is null, then the server has shutdown
	private Http4kServer engine;
	private DiskLruCache cache;

	public MangadexClient(ClientSettings clientSettings) {
		this.clientSettings = clientSettings;
		this.serverHandler = new ServerHandler(clientSettings);
		this.statistics = new AtomicReference<>();

		try {
			cache = DiskLruCache.open(new File("cache"), 1, 2,
					clientSettings.getMaxCacheSizeMib() * 1024 * 1024 /* MiB to bytes */);
		} catch (IOException e) {
			MangadexClient.dieWithError(e);
		}
	}

	// This function also does most of the program initialization.
	public void runLoop() {
		statistics.set(new Statistics());
		loginAndStartServer();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("MDNet initialization completed successfully. Starting normal operation.");
		}

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
				statistics.set(new Statistics());

				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Restarting server stopped due to hourly bandwidth limit");
				}
				if (engine == null) {
					loginAndStartServer();
				}
			} else {
				counter.set(num + 1);
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

			// if the server is offline then don't try and refresh certs
			if (engine == null) {
				return;
			}

			ServerSettings n = serverHandler.pingControl(serverSettings);
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Server settings received: {}", n);
			}

			if (n != null && (n.getTls() != null || !n.getImageServer().equals(serverSettings.getImageServer()))) {
				// certificates or upstream url must have changed, restart webserver
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Doing internal restart of HTTP server to refresh certs/upstream URL");
				}

				synchronized (shutdownLock) {
					logoutAndStopServer();
					loginAndStartServer();
				}
			}
		}, 45, 45, TimeUnit.SECONDS);

	}

	private void loginAndStartServer() {
		serverSettings = serverHandler.loginToControl();
		if (serverSettings == null) {
			MangadexClient.dieWithError("Failed to get a login response from server - check API secret for validity");
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
		System.out.println("Copyright (c) 2020, Mangadex");

		try {
			String file = "settings.json";
			if (args.length == 1) {
				file = args[0];
			} else if (args.length != 0) {
				MangadexClient.dieWithError("Expected one argument: path to config file, or nothing");
			}

			ClientSettings settings = new Gson().fromJson(new FileReader(file), ClientSettings.class);

			if (!ClientSettings.isSecretValid(settings.getClientSecret()))
				MangadexClient.dieWithError("Config Error: API Secret is invalid, must be 52 alphanumeric characters");

			if (settings.getClientPort() == 0) {
				MangadexClient.dieWithError("Config Error: Invalid port number");
			}

			if (settings.getMaxCacheSizeMib() < 1024) {
				MangadexClient.dieWithError("Config Error: Invalid max cache size, must be >= 1024 MiB (1GiB)");
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Client settings loaded: {}", settings);
			}

			MangadexClient client = new MangadexClient(settings);
			Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));
			client.runLoop();
		} catch (FileNotFoundException e) {
			MangadexClient.dieWithError(e);
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
