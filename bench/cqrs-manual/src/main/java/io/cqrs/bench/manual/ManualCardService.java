package io.cqrs.bench.manual;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;
import io.cqrs.bench.manual.command.CardCommandHandler;
import io.cqrs.bench.manual.domain.CardRepository;
import io.cqrs.bench.manual.event.Event;
import io.cqrs.bench.manual.event.EventBus;
import io.cqrs.bench.manual.event.Listener;

import java.util.concurrent.CountDownLatch;

public class ManualCardService implements CardService {

	private static final class CounterListener implements Listener {

		private CountDownLatch	latch		= new CountDownLatch(1);

		int											count;

		int											target	= -1;

		@Override
		public void handle(Event event) {
			count++;
			if (target != -1 && count == target) {
				latch.countDown();
			}
		}

		@Override
		public boolean concurrent() {
			return false;
		}

		void setTarget(int target) {
			this.target = target;
		}

		public void await() {
			try {
				latch.await();
			} catch (InterruptedException e) {
			}
		}

		@Override
		public void close() {
		}

	}

	private int							target	= 0;
	private CounterListener	list		= new CounterListener();

	@Override
	public void start() {
		EventBus.start();
		EventBus.register(list);
	}

	@Override
	public String handle(CreateCard cc) {
		target++;
		return CardCommandHandler.handle(cc);
	}

	@Override
	public String handle(DoAuthorization da) {
		target++;
		return CardCommandHandler.handle(da);
	}

	@Override
	public void clear() {
		CardRepository.clear();
	}

	@Override
	public void stop() {
		list.setTarget(target);
		list.await();
		EventBus.stop();
	}

	@Override
	public boolean parallel() {
		return false;
	}

	@Override
	public String getName() {
		return "CQRS";
	}
}
