package io.cqrs.bench.api;

public class DoAuthorization {

	private final String	pan;

	private final String	embossedDate;

	private final long		amount;

	public DoAuthorization(String pan, String embossedDate, long amount) {
		this.pan = pan;
		this.embossedDate = embossedDate;
		this.amount = amount;
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

}
