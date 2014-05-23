package io.cqrs.bench.manual.event.bus;

import io.cqrs.bench.manual.event.Event;
import io.cqrs.bench.manual.event.Listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class AsyncBus extends ListenerEventBus {

	private static enum State {
		STARTED, STARTING, STOPPING, STOPPED;
	}

	private final AtomicReference<State>	state					= new AtomicReference<>(State.STOPPED);

	// concurrent listeners can handle events concurrently
	private static ExecutorService				concurrentEs	= null;

	// non concurrent listeners needs events always coming from the same thread
	private static ExecutorService				monoEs				= null;

	protected void doFire(Event event) {
		if (State.STOPPED == state.get()) {
			start();
		}
		for (Listener l : listeners)
			(l.concurrent() ? concurrentEs : monoEs).submit(new Task(l, event));
	}

	private static final class Task implements Runnable {

		private Listener	l;
		private Event			e;

		public Task(Listener l, Event e) {
			this.l = l;
			this.e = e;
		}

		@Override
		public void run() {
			l.handle(e);
		}

	}

	protected synchronized void doStop() {
		switch (state.get()) {
		case STOPPED:
			return;
		case STARTED:
			state.set(State.STOPPING);
			concurrentEs.shutdown();
			monoEs.shutdown();
			try {
				concurrentEs.awaitTermination(100, TimeUnit.SECONDS);
				monoEs.awaitTermination(100, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (Listener list : listeners) {
				try {
					list.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			listeners.clear();
			state.set(State.STOPPED);
		default:
			stop();
			return;
		}
	}

	public synchronized void doStart() {
		switch (state.get()) {
		case STARTED:
			return;
		case STOPPED:
			state.set(State.STARTING);
			concurrentEs = Executors.newFixedThreadPool(4);
			monoEs = Executors.newSingleThreadExecutor();
			reloadListeners();
			state.set(State.STARTED);
			return;
		default:
			start();
			return;
		}
	}

}
