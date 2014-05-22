package authorization.cqrs.domain;

public class UnknownCard extends Exception {

	private static final long	serialVersionUID	= 2784614937301879586L;

	public UnknownCard(String key) {
		super("The card '" + key + "' does not exist");
	}

}
