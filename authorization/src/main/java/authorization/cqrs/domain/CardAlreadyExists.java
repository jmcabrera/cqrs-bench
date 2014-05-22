package authorization.cqrs.domain;

public class CardAlreadyExists extends Exception {

	private static final long	serialVersionUID	= -779551494237876344L;

	public CardAlreadyExists(String key) {
		super("The card '" + key + "' already exists");
	}

}
