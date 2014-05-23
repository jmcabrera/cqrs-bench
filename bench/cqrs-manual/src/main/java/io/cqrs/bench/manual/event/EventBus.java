package io.cqrs.bench.manual.event;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class EventBus {

	private static enum State {
		STARTED, STARTING, STOPPING, STOPPED;
	}

	private static final AtomicReference<State>		STATE							= new AtomicReference<>(State.STOPPED);

	private static final ServiceLoader<Listener>	SERVICE_LOADER		= ServiceLoader.load(Listener.class);

	private static final List<Listener>						LISTENERS					= new CopyOnWriteArrayList<>();

	// concurrent listeners can handle events concurrently
	private static ExecutorService								CONCURRENT_ES			= null;

	// non concurrent listeners needs events always coming from the same thread
	private static ExecutorService								NON_CONCURRENT_ES	= null;

	public static void register(Listener listener) {
		LISTENERS.add(listener);
	}

	public static void unregister(Listener listener) {
		LISTENERS.remove(listener);
	}

	public static void fire(Event event) {
		if (State.STOPPED == STATE.get()) {
			start();
		}
		for (Listener l : LISTENERS)
			(l.concurrent() ? CONCURRENT_ES : NON_CONCURRENT_ES).submit(new Task(l, event));
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

	public static void stop() {
		switch (STATE.get()) {
		case STOPPED:
			return;
		case STARTED:
			STATE.set(State.STOPPING);
			CONCURRENT_ES.shutdown();
			NON_CONCURRENT_ES.shutdown();
			try {
				CONCURRENT_ES.awaitTermination(100, TimeUnit.SECONDS);
				NON_CONCURRENT_ES.awaitTermination(100, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			for (Listener list : LISTENERS) {
				try {
					list.close();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
			LISTENERS.clear();
			STATE.set(State.STOPPED);
		default:
			stop();
			return;
		}
	}

	public static synchronized void start() {
		switch (STATE.get()) {
		case STARTED:
			return;
		case STOPPED:
			STATE.set(State.STARTING);
			CONCURRENT_ES = Executors.newFixedThreadPool(4);
			NON_CONCURRENT_ES = Executors.newSingleThreadExecutor();
			SERVICE_LOADER.reload();
			Iterator<Listener> it = SERVICE_LOADER.iterator();
			while (it.hasNext()) {
				register(it.next());
			}
			STATE.set(State.STARTED);
			return;
		default:
			start();
			return;
		}
	}
}
