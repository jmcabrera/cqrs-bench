package io.cqrs.bench.manual.domain;

import io.cqrs.bench.manual.event.Event;

public class CardCreated extends Event {

	private final String	pan;
	private final String	embossedDate;
	private final String	toString;

	public CardCreated(String pan, String embossedDate) {
		this.pan = pan;
		this.embossedDate = embossedDate;
		this.toString="CardCreated(pan='"+pan+"',embossed='"+embossedDate+"')";
	}

	public String getPan() {
		return pan;
	}

	public String getEmbossedDate() {
		return embossedDate;
	}
	
	@Override
	public String toString() {
		return toString;
	}

}
