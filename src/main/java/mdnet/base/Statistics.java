package mdnet.base;

import com.google.gson.annotations.SerializedName;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {
	@SerializedName("requests_served")
	private final AtomicInteger requestsServed;
	@SerializedName("cache_hits")
	private final AtomicInteger cacheHits;
	@SerializedName("cache_misses")
	private final AtomicInteger cacheMisses;
	@SerializedName("bytes_sent")
	private final AtomicLong bytesSent;
	@SerializedName("sequence_number")
	private final int sequenceNumber;

	public Statistics(int sequenceNumber) {
		requestsServed = new AtomicInteger();
		cacheHits = new AtomicInteger();
		cacheMisses = new AtomicInteger();
		bytesSent = new AtomicLong();
		this.sequenceNumber = sequenceNumber;
	}

	public AtomicInteger getRequestsServed() {
		return requestsServed;
	}

	public AtomicInteger getCacheHits() {
		return cacheHits;
	}

	public AtomicInteger getCacheMisses() {
		return cacheMisses;
	}

	public AtomicLong getBytesSent() {
		return bytesSent;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	@Override
	public String toString() {
		return "Statistics{" +
				"requestsServed=" + requestsServed +
				", cacheHits=" + cacheHits +
				", cacheMisses=" + cacheMisses +
				", bytesSent=" + bytesSent +
				", sequenceNumber=" + sequenceNumber +
				'}';
	}
}
