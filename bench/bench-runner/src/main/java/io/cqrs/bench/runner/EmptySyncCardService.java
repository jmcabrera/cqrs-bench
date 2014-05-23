package io.cqrs.bench.runner;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;

public class EmptySyncCardService implements CardService, Empty {

	@Override
	public void start() {}

	@Override
	public String handle(CreateCard cc) {
		return "00";
	}

	@Override
	public String handle(DoAuthorization da) {
		return "00";
	}

	@Override
	public void clear() {}

	@Override
	public void stop() {}

	@Override
	public boolean parallel() {
		return false;
	}

	@Override
	public String getName() {
		return "sEMPTY";
	}

}
