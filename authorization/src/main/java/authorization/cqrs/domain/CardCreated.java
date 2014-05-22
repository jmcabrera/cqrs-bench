package authorization.cqrs.domain;

import authorization.cqrs.event.Event;

public class CardCreated extends Event {

	private final String	pan;
	private final String	embossedDate;

	public CardCreated(String pan, String embossedDate) {
		this.pan = pan;
		this.embossedDate = embossedDate;
	}

	public String getPan() {
		return pan;
	}

	public String getEmbossedDate() {
		return embossedDate;
	}

}
