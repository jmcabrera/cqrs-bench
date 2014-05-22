package authorization.cqrs.event;

public interface Listener {

	public void handle(Event event);

	public boolean concurrent();

}
