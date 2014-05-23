package io.cqrs.bench.api;

public interface CardService {

	public void start();

	public String handle(CreateCard cc);

	public String handle(DoAuthorization da);

	public void clear();

	public void stop();

	/**
	 * Can be run in parallel
	 * 
	 * @return <code>true</code> if the service can be called in parallel,
	 *         <code>false</code> if calls must be sequential
	 */
	public boolean parallel();

	public String getName();
}
