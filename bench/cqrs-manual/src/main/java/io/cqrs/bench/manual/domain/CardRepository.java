package io.cqrs.bench.manual.domain;

import java.util.HashMap;
import java.util.Map;

public class CardRepository {

	private static final Map<String, Card>	REPO	= new HashMap<>();

	public static void clear() {
		REPO.clear();
	}

	public static void store(Card card) throws CardAlreadyExists {
		String key = card.getPan() + "/" + card.getEmbossedDate();
		if (REPO.containsKey(key))
			throw new CardAlreadyExists(key);
		REPO.put(key, card);
	}

	public static void update(Card card) throws UnknownCard {
		if (null == card)
			throw new UnknownCard(null);
		String key = card.getPan() + "/" + card.getEmbossedDate();
		if (!REPO.containsKey(key))
			throw new UnknownCard(key);
		REPO.put(key, card);
	}

	public static void delete(Card card) throws UnknownCard {
		String key = card.getPan() + "/" + card.getEmbossedDate();
		if (!REPO.containsKey(key))
			throw new UnknownCard(key);
		REPO.remove(key);
	}

	public static Card find(String pan, String embossedDate) {
		return REPO.get(pan + "/" + embossedDate);
	}
}
