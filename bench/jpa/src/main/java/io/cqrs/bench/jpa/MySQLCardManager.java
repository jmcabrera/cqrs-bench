package io.cqrs.bench.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class MySQLCardManager extends CardManager {

	@PersistenceContext(unitName = "mysql")
	private EntityManager	em;

	@Override
	protected EntityManager getEm() {
		return em;
	}

}
