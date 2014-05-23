package io.cqrs.bench.jdbc;

public class Card {

	private final String	pan;

	private final String	embossedDate;

	private long					authorizedAmount;

	public Card(String pan, String embossedDate) {
		this.pan = pan;
		this.embossedDate = embossedDate;
	}

	public String getPan() {
		return pan;
	}

	public String getEmbossedDate() {
		return embossedDate;
	}

	public long getAuthorizedAmount() {
		return authorizedAmount;
	}

	public void setAuthorizedAmount(long authorizedAmount) {
		this.authorizedAmount = authorizedAmount;
	}

}
