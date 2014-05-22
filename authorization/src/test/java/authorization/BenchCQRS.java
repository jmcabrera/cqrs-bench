package authorization;

import static authorization.Bench.TestType.AUTHORIZATION;
import static authorization.Bench.TestType.CARD_CREATION;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import authorization.cqrs.command.CardCommandHandler;
import authorization.cqrs.command.CreateCard;
import authorization.cqrs.command.DoAuthorization;
import authorization.cqrs.domain.Authorized;
import authorization.cqrs.domain.CardCreated;
import authorization.cqrs.domain.CardRepository;
import authorization.cqrs.event.Event;
import authorization.cqrs.event.EventBus;
import authorization.cqrs.event.Listener;

public class BenchCQRS extends Bench {

	private static final class CounterListener implements Listener {

		private CountDownLatch	latch;
		private Class<?>				eventType;
		int											count;

		CounterListener(CountDownLatch latch, Class<?> eventType, int count) {
			this.latch = latch;
			this.eventType = eventType;
			this.count = count;
		}

		@Override
		public void handle(Event event) {
			if (eventType.isInstance(event))
				count--;
			if (count == 0) {
				latch.countDown();
			}
		}

		@Override
		public boolean concurrent() {
			return false;
		}

		@Override
		public void close() {
		}

	}

	@Test
	public void testCQRS() throws InterruptedException {
		System.out.println("... warmup ...");
		CardRepository.clear();
		round(false);
		CardRepository.clear();
		round(true);
	}

	private void round(boolean track) throws InterruptedException {
		{
			CountDownLatch latch = new CountDownLatch(1);
			CounterListener listener = new CounterListener(latch, CardCreated.class, CARD_CREATION.nb);
			EventBus.register(listener);
			if (track)
				start("CQRS", CARD_CREATION);
			for (int i = 0; i < CARD_CREATION.nb; i++) {
				assertEquals("00", CardCommandHandler.handle(new CreateCard("" + i, "0514")));
			}
			latch.await();
			if (track)
				end();
			EventBus.unregister(listener);
		}

		{
			CountDownLatch latch = new CountDownLatch(1);
			CounterListener listener = new CounterListener(latch, Authorized.class, AUTHORIZATION.nb);
			EventBus.register(listener);
			if (track)
				start("CQRS", AUTHORIZATION);
			for (int i = 0; i < AUTHORIZATION.nb; i++) {
				assertEquals("00", CardCommandHandler.handle(new DoAuthorization("" + (i % CARD_CREATION.nb), "0514", 1)));
			}
			latch.await();
			if (track)
				end();
			EventBus.unregister(listener);
		}
	}
}
