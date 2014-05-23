package io.cqrs.bench.runner;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class BencherTest {

	private static final int	CARD_CREATION	= 100;
	private static final int	AUTHORIZATION	= 1000;

	private static abstract class Operation implements Runnable {
		protected CardService	serv;

		void setCardService(CardService serv) {
			this.serv = serv;
		}

	}

	public static void main(String[] args) {
		// Planning operations
		final Operation[] creations = new Operation[CARD_CREATION];
		final Operation[] authorizations = new Operation[AUTHORIZATION];

		for (int i = 0; i < creations.length; i++) {
			final int j = i;
			creations[i] = new Operation() {
				@Override
				public void run() {
					serv.handle(new CreateCard("" + j, "0514"));
				}
			};
		}

		for (int i = 0; i < authorizations.length; i++) {
			final int j = i;
			authorizations[i] = new Operation() {
				@Override
				public void run() {
					serv.handle(new CreateCard("" + j, "0514"));
				}
			};
		}

		int found = 0;
		{
			Map<String, List<CardService>> css = new HashMap<>();
			for (CardService cs : ServiceLoader.load(CardService.class)) {
				found++;
				String name = cs.getName();
				List<CardService> list = css.get(name);
				if (null == list) {
					list = new ArrayList<>();
					css.put(name, list);
				}
				list.add(cs);
			}
			if (0 == found) {
				System.err.println("Found nothing to run... Please check that:\n" + //
						" - you have placed at least one implementation in the classpath\n" + //
						" - each implementation define a '/META-INF/services/" + CardService.class.getName() + "' file as per the ServiceLoader API specifications");
			}
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, List<CardService>> e : css.entrySet()) {
				if (e.getValue().size() > 1) {
					sb.append("For name :").append(e.getKey()).append("\n");
					for (CardService cs : e.getValue()) {
						sb.append(" - ")//
								.append(cs.getClass().getSimpleName())//
								.append(" from ")//
								.append(cs.getClass().getPackage().getName())//
								.append("\n");
					}
				}
			}

			if (0 != sb.length()) {
				System.err.println("Several implementations share the same name:\n" + sb);
				return;
			}

		}

		for (CardService cs : ServiceLoader.load(CardService.class)) {
			run(cs, creations, authorizations);
		}

		System.out.println("\n\n-------------------------------------------------------\n"//
				+ "NB:\n"//
				+ "If your implementation was not found, please check that:\n" + //
				" - you have placed at least one implementation in the classpath\n" + //
				" - each implementation define a '/META-INF/services/" + CardService.class.getName() + "' file as per the ServiceLoader API specifications");
	}

	private static void run(CardService cs, Operation[] creations, Operation[] authorizations) {
		System.out.printf("Running: %7s    # %20s from %-30s\n", cs.getName(), cs.getClass().getSimpleName(), cs.getClass().getPackage().getName());

		cs.start();
		if (cs.parallel()) {
			ExecutorService es = new ForkJoinPool();
			System.out.println("... Warmup ...");
			runAsync(cs, es, creations);
			runAsync(cs, es, authorizations);
			System.out.println("--------------");
			cs.clear();
			runAsync(cs, es, creations);
			runAsync(cs, es, authorizations);
			es.shutdown();
			try {
				es.awaitTermination(1, TimeUnit.DAYS);
			} catch (InterruptedException e) {
			}
		} else {
			System.out.println("... Warmup ...");
			runSync(cs, creations);
			runSync(cs, authorizations);
			System.out.println("--------------");
			cs.clear();
			runSync(cs, creations);
			runSync(cs, authorizations);
		}
		cs.stop();
	}

	private static void runSync(CardService cs, Operation[] operations) {
		for (Operation op : operations) {
			op.setCardService(cs);
			op.run();
		}
	}

	private static void runAsync(CardService cs, ExecutorService es, Operation[] operations) {
		Future<?>[] res = new Future<?>[operations.length];
		for (int i = 0; i < operations.length; i++) {
			operations[i].setCardService(cs);
			res[i] = es.submit(operations[i]);
		}
		for (Future<?> f : res) {
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	// public void testCQRS() throws InterruptedException {
	// System.out.println("... warmup ...");
	// CardRepository.clear();
	// round(false);
	// CardRepository.clear();
	// round(true);
	// }
	//
	// private void round(boolean track) throws InterruptedException {
	// {
	// CountDownLatch latch = new CountDownLatch(1);
	// CounterListener listener = new CounterListener(latch, CardCreated.class,
	// CARD_CREATION.nb);
	// EventBus.register(listener);
	// if (track)
	// start("CQRS", CARD_CREATION);
	// for (int i = 0; i < CARD_CREATION.nb; i++) {
	// assertEquals("00", CardCommandHandler.handle(new CreateCard("" + i,
	// "0514")));
	// }
	// latch.await();
	// if (track)
	// end();
	// EventBus.unregister(listener);
	// }
	//
	// {
	// CountDownLatch latch = new CountDownLatch(1);
	// CounterListener listener = new CounterListener(latch, Authorized.class,
	// AUTHORIZATION.nb);
	// EventBus.register(listener);
	// if (track)
	// start("CQRS", AUTHORIZATION);
	// for (int i = 0; i < AUTHORIZATION.nb; i++) {
	// assertEquals("00", CardCommandHandler.handle(new DoAuthorization("" + (i %
	// CARD_CREATION.nb), "0514", 1)));
	// }
	// latch.await();
	// if (track)
	// end();
	// EventBus.unregister(listener);
	// }
	// }
	//
	// private static final Runnable[] creations = new Runnable[CARD_CREATION.nb];
	// private static final Runnable[] authorizations = new
	// Runnable[AUTHORIZATION.nb];
	//
	// static {
	// for (int i = 0; i < creations.length; i++) {
	// final int j = i;
	// creations[i] = new Runnable() {
	// @Override
	// public void run() {
	// assertEquals("00", CardManager.createCard(new Card("" + j, "0514")));
	// }
	// };
	// }
	//
	// for (int i = 0; i < authorizations.length; i++) {
	// final int j = i;
	// authorizations[i] = new Runnable() {
	// @Override
	// public void run() {
	// assertEquals("00", CardManager.authorization("" + j % CARD_CREATION.nb,
	// "0514", 1));
	// }
	// };
	// }
	// }
	//
	// @Test
	// public void testJDBCMulti() throws InterruptedException {
	// System.out.println("... warmup ...");
	// CardRepository.clear();
	// round(false);
	// CardRepository.clear();
	// round(true);
	// }
	//
	// private void round(boolean track) throws InterruptedException {
	// {
	// ExecutorService es = new ForkJoinPool(PARALLELISM);
	// if (track)
	// start("JDBC", CARD_CREATION);
	// for (Runnable creation : creations) {
	// es.execute(creation);
	// }
	// es.shutdown();
	// es.awaitTermination(1, TimeUnit.DAYS);
	// if (track)
	// end();
	// }
	//
	// {
	// ExecutorService es = new ForkJoinPool(PARALLELISM);
	// if (track)
	// start("JDBC", AUTHORIZATION);
	// for (Runnable authorization : authorizations) {
	// es.execute(authorization);
	// }
	// es.shutdown();
	// es.awaitTermination(1, TimeUnit.DAYS);
	// if (track)
	// end();
	// }
	// }

}
