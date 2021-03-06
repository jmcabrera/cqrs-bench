package io.cqrs.bench.jdbc;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;

public class JDBCCardService implements CardService {

	@Override
	public void start() {}

	@Override
	public String handle(CreateCard cc) {
		Card c = new Card(cc.getPan(), cc.getEmbossedDate());
		return CardManager.createCard(c);
	}

	@Override
	public String handle(DoAuthorization da) {
		return CardManager.authorization(da.getPan(), da.getEmbossedDate(), da.getAmount());
	}

	@Override
	public void clear() {
		CardRepository.clear();
	}

	@Override
	public void stop() {}

	@Override
	public boolean parallel() {
		return false;
	}

	@Override
	public String getName() {
		return "JDBC";
	}
}
