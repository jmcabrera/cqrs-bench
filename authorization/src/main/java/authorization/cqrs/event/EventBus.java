package authorization.cqrs.event;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EventBus {

	private static final List<Listener>		LISTENERS					= new CopyOnWriteArrayList<>();

	static {
		ServiceLoader<Listener> sl = ServiceLoader.load(Listener.class);
		Iterator<Listener> it = sl.iterator();
		while (it.hasNext()) {
			register(it.next());
		}
	}

	// concurrent listeners can handle events concurrently
	private static final ExecutorService	CONCURRENT_ES			= Executors.newFixedThreadPool(4);

	// non concurrent listeners needs events always coming from the same thread
	private static final ExecutorService	NON_CONCURRENT_ES	= Executors.newSingleThreadExecutor();

	public static void register(Listener listener) {
		LISTENERS.add(listener);
	}

	public static void unregister(Listener listener) {
		if (!LISTENERS.remove(listener)) {
			System.out.println("Unregistering " + listener.getClass().getSimpleName() + " (not found, no-op)");
		}
	}

	public static void fire(Event event) {
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
	}

}
