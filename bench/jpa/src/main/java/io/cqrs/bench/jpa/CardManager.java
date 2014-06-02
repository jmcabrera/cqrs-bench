package io.cqrs.bench.jpa;

import io.cqrs.bench.jpa.Card.CardId;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;

public abstract class CardManager {

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void createCard(String pan, String embossedDate) {
		Card c = new Card(pan, embossedDate);
		getEm().persist(c);
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void authorize(String pan, String embossedDate, long amount) {
//		int retry = 5;
//		while (retry > 0) {
			try {
				Card c = getEm().find(Card.class, new CardId(pan, embossedDate));
				c.setAuthorizedAmount(c.getAuthorizedAmount() + amount);
				getEm().flush();
				return;
			} catch (NullPointerException | OptimisticLockException e) {
//				if (retry < 4) {
//					System.out.println("[" + retry + "] retrying pan " + pan);
//				}
			}
//			retry--;
//		}
//		throw new RuntimeException("Aborting after 5 retries");
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void clear() {
		for (Object o : getEm().createNamedQuery("findAll").getResultList()) {
			getEm().remove(o);
		}
		getEm().getEntityManagerFactory().getCache().evictAll();
	}

	protected abstract EntityManager getEm();

}
