package io.cqrs.bench.manual.event.bus;

import io.cqrs.bench.manual.event.Event;
import io.cqrs.bench.manual.event.Listener;

import java.util.concurrent.atomic.AtomicReference;

public class SyncBus extends ListenerEventBus {

	private static enum State {
		STARTED, STARTING, STOPPING, STOPPED;
	}

	private final AtomicReference<State>	state	= new AtomicReference<>(State.STOPPED);

	protected void doFire(Event event) {
		if (State.STOPPED == state.get())
			start();
		for (Listener l : listeners)
			l.handle(event);
	}

	protected synchronized void doStop() {
		switch (state.get()) {
		case STOPPED:
			return;
		case STARTED:
			state.set(State.STOPPING);
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
			reloadListeners();
			state.set(State.STARTED);
			return;
		default:
			start();
			return;
		}
	}

}
