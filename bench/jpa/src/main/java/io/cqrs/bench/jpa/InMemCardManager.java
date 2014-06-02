package io.cqrs.bench.jpa;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class InMemCardManager extends CardManager {

	@PersistenceContext(unitName = "inmem")
	private EntityManager	em;

	@Override
	public EntityManager getEm() {
		return em;
	}

}
