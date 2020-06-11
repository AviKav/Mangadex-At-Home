package mdnet.base.settings;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;
import java.util.regex.Pattern;

public final class ClientSettings {
	@SerializedName("max_cache_size_mib")
	private final long maxCacheSizeMib;
	@SerializedName("max_bandwidth_mib_per_hour")
	private final long maxBandwidthMibPerHour;
	@SerializedName("max_burst_rate_kib_per_second")
	private final long maxBurstRateKibPerSecond;
	@SerializedName("client_port")
	private final int clientPort;
	@SerializedName("client_secret")
	private final String clientSecret;
	@SerializedName("threads")
	private final int threads;
	@SerializedName("web_settings")
	private final WebSettings webSettings;

	public ClientSettings() {
		this.maxCacheSizeMib = 20480;
		this.maxBandwidthMibPerHour = 0;
		this.maxBurstRateKibPerSecond = 0;
		this.clientPort = 1200;
		this.clientSecret = "PASTE-YOUR-SECRET-HERE";
		this.threads = 32;
		this.webSettings = new WebSettings();
	}

	public ClientSettings(long maxCacheSizeMib, long maxBandwidthMibPerHour, long maxBurstRateKibPerSecond,
			int clientPort, String clientSecret, int threads, WebSettings webSettings) {
		this.maxCacheSizeMib = maxCacheSizeMib;
		this.maxBandwidthMibPerHour = maxBandwidthMibPerHour;
		this.maxBurstRateKibPerSecond = maxBurstRateKibPerSecond;
		this.clientPort = clientPort;
		this.clientSecret = Objects.requireNonNull(clientSecret);
		this.threads = threads;
		this.webSettings = webSettings;
	}

	public long getMaxCacheSizeMib() {
		return maxCacheSizeMib;
	}

	public long getMaxBandwidthMibPerHour() {
		return maxBandwidthMibPerHour;
	}

	public long getMaxBurstRateKibPerSecond() {
		return maxBurstRateKibPerSecond;
	}

	public int getClientPort() {
		return clientPort;
	}

	public String getClientSecret() {
		return clientSecret;
	}
	public WebSettings getWebSettings() {
		return webSettings;
	}

	public int getThreads() {
		return threads;
	}

	@Override
	public String toString() {
		return "ClientSettings{" + "maxCacheSizeMib=" + maxCacheSizeMib + ", maxBandwidthMibPerHour="
				+ maxBandwidthMibPerHour + ", maxBurstRateKibPerSecond=" + maxBurstRateKibPerSecond + ", clientPort="
				+ clientPort + ", clientSecret='" + "<hidden>" + '\'' + ", threads=" + getThreads() + '}';
	}

	public static boolean isSecretValid(String clientSecret) {
		final int CLIENT_KEY_LENGTH = 52;
		return Pattern.matches("^[a-zA-Z0-9]{" + CLIENT_KEY_LENGTH + "}$", clientSecret);
	}
}
