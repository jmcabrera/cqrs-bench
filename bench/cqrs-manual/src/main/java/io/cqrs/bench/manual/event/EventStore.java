package io.cqrs.bench.manual.event;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class EventStore implements Listener {

	private static final EventStore		INSTANCE	= new EventStore();

	private static final ObjectMapper	MAPPER		= new ObjectMapper();
	static {
		MAPPER.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		EventBus.register(INSTANCE);
	}

	private PrintWriter								pw				= null;
	int																sequence	= 0;

	private EventStore() {
		_restart();
	}

	private final AtomicBoolean	closed	= new AtomicBoolean(Boolean.FALSE);

	@Override
	public void handle(Event event) {
		if (closed.get()) throw new RuntimeException("Event store closed");

		try {
			pw.println((sequence++) + MAPPER.writeValueAsString(event));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean concurrent() {
		return false;
	}

	@Override
	public void close() {
		closed.set(true);
		if (null != pw) {
			pw.close();
		}
	}

	public static void restart() {
		INSTANCE._restart();
	}

	private void _restart() {
		closed.set(true);
		close();
		try {
			pw = new PrintWriter(new GZIPOutputStream(new FileOutputStream(new File("store.json"))), false);
			sequence = 0;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		closed.set(false);
	}

}
