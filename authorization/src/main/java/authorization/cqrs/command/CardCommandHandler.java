package authorization.cqrs.command;

import authorization.cqrs.domain.Card;
import authorization.cqrs.domain.CardAlreadyExists;
import authorization.cqrs.domain.CardRepository;
import authorization.cqrs.domain.UnknownCard;

public class CardCommandHandler {

	public static String handle(CreateCard create) {
		try {
			CardRepository.store(new Card(create.getPan(), create.getEmbossedDate()));
			return "00"; // OK
		} catch (CardAlreadyExists e) {
			return "61"; // Duplicate card
		}
	}

	public static String handle(DoAuthorization auth) {
		Card card = CardRepository.find(auth.getPan(), auth.getEmbossedDate());
		try {
			if (null != card) {
				card.authorize(auth.getAmount());
				return "00"; // OK
			} else {
				return "60"; // Card unknown
			}
		} catch (Throwable t) {
			return "99"; // TODO accomodate according to the protocol
		} finally {
			try {
				CardRepository.update(card);
			} catch (UnknownCard e) {
				// should not happen
				return "99"; // TODO accomodate according to the protocol
			}
		}
	}

}
