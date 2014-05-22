package authorization.cqrs.event;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class EventStore implements Listener {

	static final PrintWriter	PW;
	static final ObjectMapper	MAPPER;
	static int								sequence	= 0;

	static {
		try {
			PW = new PrintWriter(new FileWriter(new File("store.json")), true);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		MAPPER = new ObjectMapper();
		MAPPER.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
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

}
