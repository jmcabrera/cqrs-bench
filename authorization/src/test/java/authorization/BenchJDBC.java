package authorization;

import static authorization.Bench.TestType.AUTHORIZATION;
import static authorization.Bench.TestType.CARD_CREATION;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import authorization.jdbc.Card;
import authorization.jdbc.CardManager;
import authorization.jdbc.CardRepository;

public class BenchJDBC extends Bench {

	private static final Runnable[]	creations				= new Runnable[CARD_CREATION.nb];
	private static final Runnable[]	authorizations	= new Runnable[AUTHORIZATION.nb];

	static {
		for (int i = 0; i < creations.length; i++) {
			final int j = i;
			creations[i] = new Runnable() {
				@Override
				public void run() {
					assertEquals("00", CardManager.createCard(new Card("" + j, "0514")));
				}
			};
		}

		for (int i = 0; i < authorizations.length; i++) {
			final int j = i;
			authorizations[i] = new Runnable() {
				@Override
				public void run() {
					assertEquals("00", CardManager.authorization("" + j % CARD_CREATION.nb, "0514", 1));
				}
			};
		}
	}

	@Test
	public void testJDBCMulti() throws InterruptedException {
		System.out.println("... warmup ...");
		CardRepository.clear();
		round(false);
		CardRepository.clear();
		round(true);
	}

	private void round(boolean track) throws InterruptedException {
		{
			ExecutorService es = new ForkJoinPool(PARALLELISM);
			if (track)
				start("JDBC", CARD_CREATION);
			for (Runnable creation : creations) {
				es.execute(creation);
			}
			es.shutdown();
			es.awaitTermination(1, TimeUnit.DAYS);
			if (track)
				end();
		}

		{
			ExecutorService es = new ForkJoinPool(PARALLELISM);
			if (track)
				start("JDBC", AUTHORIZATION);
			for (Runnable authorization : authorizations) {
				es.execute(authorization);
			}
			es.shutdown();
			es.awaitTermination(1, TimeUnit.DAYS);
			if (track)
				end();
		}
	}
}
