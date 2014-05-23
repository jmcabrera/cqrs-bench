package io.cqrs.bench.manual.event;

import java.util.ServiceLoader;

public abstract class EventBus {

	private static final EventBus	IMPL;
	static {
		EventBus instance = null;
		for (EventBus eb : ServiceLoader.load(EventBus.class))
			if (null == instance)
				instance = eb;
			else {
				String err = "More than one event bus implementation was provided...\n";
				for (EventBus a : ServiceLoader.load(EventBus.class))
					err += "  - " + a.getClass().getName() + "\n";
				throw new RuntimeException(err);
			}
		if (null == instance)
			throw new RuntimeException("" //
					+ "I need an implementation for Eventbus (exactly one) but I found none.\n" //
					+ "Give me one in a /META-INF/services/" + EventBus.class.getName() + "" //
					+ "somewhere in the classpath.");
		IMPL = instance;
	}

	public static void register(Listener listener) {
		IMPL.doRegister(listener);
	}

	public static void unregister(Listener listener) {
		IMPL.doUnregister(listener);
	}

	public static void fire(Event event) {
		IMPL.doFire(event);
	}

	public static void stop() {
		IMPL.doStop();
	}

	public static void start() {
		IMPL.doStart();
	}

	protected abstract void doRegister(Listener listener);

	protected abstract void doUnregister(Listener listener);

	protected abstract void doFire(Event event);

	protected abstract void doStop();

	protected abstract void doStart();
}
