package io.cqrs.bench.jpa;

import io.cqrs.bench.jpa.Card.CardId;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class CardManager {

	@PersistenceContext
	private EntityManager	em;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createCard(String pan, String embossedDate) {
		Card c = new Card(pan, embossedDate);
		em.persist(c);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void authorize(String pan, String embossedDate, long amount) {
		em.find(Card.class, new CardId(pan, embossedDate));

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void clear() {
		em.createNamedQuery("drop_all_cards").executeUpdate();
		em.clear();
	}

}
