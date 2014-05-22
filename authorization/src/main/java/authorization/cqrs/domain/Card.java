package authorization.cqrs.domain;

import authorization.cqrs.event.EventBus;

public class Card {

	private final String	pan;

	private final String	embossedDate;

	private long					authorizedAmount;

	public Card(String pan, String embossedDate) {
		this.pan = pan;
		this.embossedDate = embossedDate;
		EventBus.fire(new CardCreated(pan, embossedDate));
	}

	public void authorize(long amount) {
		authorizedAmount += amount;
		EventBus.fire(new Authorized(pan, embossedDate, amount));
	}

	public void reset() {
		authorizedAmount = 0;
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

}
