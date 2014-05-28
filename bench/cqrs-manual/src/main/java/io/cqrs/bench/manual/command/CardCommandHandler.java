package io.cqrs.bench.manual.command;

import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;
import io.cqrs.bench.manual.domain.Card;
import io.cqrs.bench.manual.domain.CardAlreadyExists;
import io.cqrs.bench.manual.domain.CardRepository;

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
			t.printStackTrace();
			return "99"; // TODO accomodate according to the protocol
		}
	}

}
