package io.cqrs.bench.manual.event.bus;

import io.cqrs.bench.manual.event.EventBus;
import io.cqrs.bench.manual.event.Listener;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

abstract class ListenerEventBus extends EventBus {

	private static final ServiceLoader<Listener>	SERVICE_LOADER	= ServiceLoader.load(Listener.class);

	protected final List<Listener>								listeners				= new CopyOnWriteArrayList<>();

	protected void doRegister(Listener listener) {
		listeners.add(listener);
	}

	protected void doUnregister(Listener listener) {
		listeners.remove(listener);
	}

	protected void reloadListeners() {
		SERVICE_LOADER.reload();
		Iterator<Listener> it = SERVICE_LOADER.iterator();
		while (it.hasNext()) {
			register(it.next());
		}
	}

}
