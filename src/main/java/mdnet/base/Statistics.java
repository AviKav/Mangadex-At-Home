package mdnet.base;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {
	private final AtomicInteger requestsServed;
	private final AtomicInteger cacheHits;
	private final AtomicInteger cacheMisses;
	private final AtomicLong bytesSent;

	public Statistics() {
		requestsServed = new AtomicInteger();
		cacheHits = new AtomicInteger();
		cacheMisses = new AtomicInteger();
		bytesSent = new AtomicLong();
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

	@Override
	public String toString() {
		return "Statistics{" + "requestsServed=" + requestsServed + ", cacheHits=" + cacheHits + ", cacheMisses="
				+ cacheMisses + ", bytesSent=" + bytesSent + '}';
	}
}
