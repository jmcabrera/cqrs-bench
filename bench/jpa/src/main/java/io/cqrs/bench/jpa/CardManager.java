package io.cqrs.bench.jpa;

import io.cqrs.bench.jpa.Card.CardId;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
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
		int retry = 5;
		while (retry > 0) {
			try {
				Card c = em.find(Card.class, new CardId(pan, embossedDate));
				c.setAuthorizedAmount(c.getAuthorizedAmount() + amount);
				em.flush();
				return;
			} catch (NullPointerException | OptimisticLockException e) {
				if(retry < 4) {
					System.out.println("[" + retry + "] retrying pan " + pan);
				}
			}
			retry--;
		}
		throw new RuntimeException("Aborting after 5 retries");
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void clear() {
		em.createNamedQuery("drop_all_cards").executeUpdate();
		em.clear();
		em.getEntityManagerFactory().getCache().evictAll();
	}

}
