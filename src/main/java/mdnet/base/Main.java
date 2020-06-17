package mdnet.base;

import mdnet.base.settings.ClientSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import static mdnet.base.Constants.GSON;

public class Main {
	private final static Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		System.out.println("Mangadex@Home Client " + Constants.CLIENT_VERSION + " (Build " + Constants.CLIENT_BUILD
				+ ") initializing\n");
		System.out.println("Copyright (c) 2020, MangaDex Network");

		String file = "settings.json";
		if (args.length == 1) {
			file = args[0];
		} else if (args.length != 0) {
			dieWithError("Expected one argument: path to config file, or nothing");
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
				dieWithError(e);
			}
		}

		validateSettings(settings);

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
			dieWithError("Config Error: API Secret is invalid, must be 52 alphanumeric characters");

		if (settings.getClientPort() == 0) {
			dieWithError("Config Error: Invalid port number");
		}

		if (settings.getMaxCacheSizeInMebibytes() < 1024) {
			dieWithError("Config Error: Invalid max cache size, must be >= 1024 MiB (1GiB)");
		}

		if (settings.getThreads() < 4) {
			dieWithError("Config Error: Invalid number of threads, must be >= 4");
		}

		if (settings.getMaxMebibytesPerHour() < 0) {
			dieWithError("Config Error: Max bandwidth must be >= 0");
		}

		if (settings.getMaxKilobitsPerSecond() < 0) {
			dieWithError("Config Error: Max burst rate must be >= 0");
		}

		if (settings.getWebSettings() != null) {
			if (settings.getWebSettings().getUiPort() == 0) {
				dieWithError("Config Error: Invalid UI port number");
			}
		}
	}

	public static boolean isSecretValid(String clientSecret) {
		final int CLIENT_KEY_LENGTH = 52;
		return Pattern.matches("^[a-zA-Z0-9]{" + CLIENT_KEY_LENGTH + "}$", clientSecret);
	}
}
