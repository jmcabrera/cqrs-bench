package io.cqrs.bench.manual.domain;

import java.util.HashMap;
import java.util.Map;

public class CardRepository {

	private static final Map<String, Card>	REPO	= new HashMap<>(1000000);

	public static void clear() {
		REPO.clear();
	}

	public static void store(Card card) throws CardAlreadyExists {
		String key = card.getPan() + "/" + card.getEmbossedDate();
		Card prev = REPO.put(key, card);
		if (null != prev) {
			REPO.put(key, prev);
			throw new CardAlreadyExists(key);
		}
	}

	public static void delete(Card card) throws UnknownCard {
		String key = card.getPan() + "/" + card.getEmbossedDate();
		Card prev = REPO.remove(key);
		if (null == prev) throw new UnknownCard(key);
	}

	public static Card find(String pan, String embossedDate) {
		return REPO.get(pan + "/" + embossedDate);
	}
}
