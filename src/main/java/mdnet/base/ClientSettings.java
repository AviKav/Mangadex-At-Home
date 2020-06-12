package mdnet.base;

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
	@SerializedName("threads_per_cpu")
	private final int threadsPerCpu;

	public ClientSettings(long maxCacheSizeMib, long maxBandwidthMibPerHour, long maxBurstRateKibPerSecond,
			int clientPort, String clientSecret, int threadsPerCpu) {
		this.maxCacheSizeMib = maxCacheSizeMib;
		this.maxBandwidthMibPerHour = maxBandwidthMibPerHour;
		this.maxBurstRateKibPerSecond = maxBurstRateKibPerSecond;
		this.clientPort = clientPort;
		this.clientSecret = Objects.requireNonNull(clientSecret);
		this.threadsPerCpu = threadsPerCpu;
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

	public int getThreadsPerCpu() {
		return threadsPerCpu;
	}

	@Override
	public String toString() {
		return "ClientSettings{" + "maxCacheSizeMib=" + maxCacheSizeMib + ", maxBandwidthMibPerHour="
				+ maxBandwidthMibPerHour + ", maxBurstRateKibPerSecond=" + maxBurstRateKibPerSecond + ", clientPort="
				+ clientPort + ", clientSecret='" + "<hidden>" + '\'' + ", threadsPerCpu=" + threadsPerCpu + '}';
	}

	public static boolean isSecretValid(String clientSecret) {
		final int CLIENT_KEY_LENGTH = 52;
		return Pattern.matches("^[a-zA-Z0-9]{" + CLIENT_KEY_LENGTH + "}$", clientSecret);
	}
}
