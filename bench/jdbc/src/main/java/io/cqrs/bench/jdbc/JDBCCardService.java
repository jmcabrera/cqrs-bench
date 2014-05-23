package io.cqrs.bench.jdbc;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;

public class JDBCCardService implements CardService {

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public String handle(CreateCard cc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String handle(DoAuthorization da) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean parallel() {
		return true;
	}

	@Override
	public String getName() {
		return "JDBC";
	}
}
