package io.cqrs.bench.manual.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class EventStore implements Listener {

	private static final ObjectMapper	MAPPER		= new ObjectMapper();
	static {
		MAPPER.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
	}

	private final PrintWriter					PW;
	int																sequence	= 0;

	public EventStore() {
		try {
			PW = new PrintWriter(new FileWriter(new File("store.json")), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void handle(Event event) {
		try {
			PW.println((sequence++) + MAPPER.writeValueAsString(event));
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
		PW.close();
		sequence = 0;
	}

}
