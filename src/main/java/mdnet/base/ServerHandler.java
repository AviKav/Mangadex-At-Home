package mdnet.base;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class ServerHandler {
	private final static Logger LOGGER = LoggerFactory.getLogger(ServerHandler.class);
	private static final String SERVER_ADDRESS = "https://mangadex-test.net/";

	private final ClientSettings settings;

	public ServerHandler(ClientSettings settings) {
		this.settings = settings;
	}

	public boolean logoutFromControl() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Disconnecting from the control server");
		}

		HashMap<String, Object> params = new HashMap<>();
		params.put("secret", settings.getClientSecret());

		HttpResponse<?> json = Unirest.post(SERVER_ADDRESS + "stop").header("Content-Type", "application/json")
				.body(new JSONObject(params)).asEmpty();

		return json.isSuccess();
	}

	public ServerSettings loginToControl() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Connecting to the control server");
		}

		HashMap<String, Object> params = new HashMap<>();
		params.put("secret", settings.getClientSecret());
		params.put("port", settings.getClientPort());
		params.put("disk_space", settings.getMaxCacheSizeMib() * 1024 * 1024 /* MiB to bytes */);

		HttpResponse<ServerSettings> response = Unirest.post(SERVER_ADDRESS + "ping")
				.header("Content-Type", "application/json").body(new JSONObject(params)).asObject(ServerSettings.class);

		if (response.isSuccess()) {
			return response.getBody();
		} else {
			// unirest deserializes errors into an object with all null fields instead of a
			// null object
			return null;
		}
	}

	public ServerSettings pingControl(ServerSettings old) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Pinging the control server");
		}

		HashMap<String, Object> params = new HashMap<>();
		params.put("secret", settings.getClientSecret());
		params.put("port", settings.getClientPort());
		params.put("disk_space", settings.getMaxCacheSizeMib() * 1024 * 1024 /* MiB to bytes */);
		params.put("tls_created_at", old.getTls().getCreatedAt());

		HttpResponse<ServerSettings> response = Unirest.post(SERVER_ADDRESS + "ping")
				.header("Content-Type", "application/json").body(new JSONObject(params)).asObject(ServerSettings.class);

		if (response.isSuccess()) {
			return response.getBody();
		} else {
			// unirest deserializes errors into an object with all null fields instead of a
			// null object
			return null;
		}
	}
}
