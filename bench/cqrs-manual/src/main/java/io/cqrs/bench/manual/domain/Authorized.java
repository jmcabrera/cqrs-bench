package io.cqrs.bench.manual.domain;

import io.cqrs.bench.manual.event.Event;

public class Authorized extends Event {

	private final String	pan;
	private final String	embossedDate;
	private final long		amount;
	private String				toString;

	public Authorized(String pan, String embossedDate, long amount) {
		this.pan = pan;
		this.embossedDate = embossedDate;
		this.amount = amount;
		this.toString = "Authorized(" + pan + "," + embossedDate + "," + amount + "," + timestamp + ")";
	}

	public String getPan() {
		return pan;
	}

	public String getEmbossedDate() {
		return embossedDate;
	}

	public long getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return toString;
	}
}
