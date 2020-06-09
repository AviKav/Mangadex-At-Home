package mdnet.base;

import java.time.Duration;

public class Constants {
	public static final int CLIENT_BUILD = 2;
	public static final String CLIENT_VERSION = "1.0";
	public static final Duration MAX_AGE_CACHE = Duration.ofDays(14);

	public static final int MAX_CONCURRENT_CONNECTIONS = 2;
	public static final String OVERLOADED_MESSAGE = "This server is experiencing a surge in connections. Please try again later.";
}
