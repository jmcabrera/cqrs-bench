package io.cqrs.bench.jpa;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JPACardService implements CardService {

	private static InitialContext									INITIAL_CONTEXT	= null;
	private static String													BEAN_NAME				= null;

	@Override
	public void start() {
		try {
			INITIAL_CONTEXT = new InitialContext(new Properties());
			INITIAL_CONTEXT.lookup("java:global/classpath.ear/jpa-0.0.1-SNAPSHOT/CardManager");
			BEAN_NAME = "java:global/classpath.ear/jpa-0.0.1-SNAPSHOT/CardManager";
		} catch (NamingException e) {
			System.out.println("got " + e.getMessage() + ", trying alt name.");
			try {
				INITIAL_CONTEXT.lookup("java:global/classpath.ear/jpa/CardManager");
				BEAN_NAME = "java:global/classpath.ear/jpa/CardManager";
			} catch (NamingException e1) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public String handle(CreateCard cc) {
		try {
			CardManager cm = (CardManager) INITIAL_CONTEXT.lookup(BEAN_NAME);
			cm.createCard(cc.getPan(), cc.getEmbossedDate());
			return "00";
		} catch (NamingException e) {
			e.printStackTrace();
			return "99";
		}
	}

	@Override
	public String handle(DoAuthorization da) {
		try {
			CardManager cm = (CardManager) INITIAL_CONTEXT.lookup(BEAN_NAME);
			cm.authorize(da.getPan(), da.getEmbossedDate(), da.getAmount());
			return "00";
		} catch (NamingException e) {
			e.printStackTrace();
			return "99";
		}
	}

	@Override
	public void clear() {
		try {
			CardManager cm = (CardManager) INITIAL_CONTEXT.lookup(BEAN_NAME);
			cm.clear();
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
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
