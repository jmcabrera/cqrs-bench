package io.cqrs.bench.jpa;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JPACardService implements CardService {

	private static InitialContext	INITIAL_CONTEXT	= null;
	private static CardManager		BEAN						= null;

	@Override
	public void start() {
		try {
			INITIAL_CONTEXT = new InitialContext(new Properties());
			BEAN = (CardManager) INITIAL_CONTEXT.lookup("java:global/classpath.ear/jpa-0.0.1-SNAPSHOT/CardManager");
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String handle(CreateCard cc) {
		BEAN.createCard(cc.getPan(), cc.getEmbossedDate());
		return "00";
	}

	@Override
	public String handle(DoAuthorization da) {
		BEAN.authorize(da.getPan(), da.getEmbossedDate(), da.getAmount());
		return "00";
	}

	@Override
	public void clear() {
		BEAN.clear();
	}

	@Override
	public void stop() {
		BEAN = null;
		try {
			INITIAL_CONTEXT.close();
			INITIAL_CONTEXT = null;
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean parallel() {
		return true;
	}

	@Override
	public String getName() {
		return "JPA";
	}

}
