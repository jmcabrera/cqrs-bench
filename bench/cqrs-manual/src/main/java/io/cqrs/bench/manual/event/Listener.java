package io.cqrs.bench.manual.event;

public interface Listener {

	public void handle(Event event);

	public boolean concurrent();

	public void close();

}