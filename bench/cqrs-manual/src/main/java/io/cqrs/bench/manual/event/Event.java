package io.cqrs.bench.manual.event;

public abstract class Event {

	protected final long	timestamp	= System.currentTimeMillis();

	public long getTimestamp() {
		return timestamp;
	}

}
