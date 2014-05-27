package io.cqrs.bench.api;

public class CreateCard {

	private final String	pan;
	private final String	embossedDate;

	public CreateCard(String pan, String embossedDate) {
		this.pan = pan;
		this.embossedDate = embossedDate;
	}

	public String getPan() {
		return pan;
	}

	public String getEmbossedDate() {
		return embossedDate;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(pan='" + pan + "', embossedDate='" + embossedDate + "')";
	}
}
