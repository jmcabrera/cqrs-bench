package io.cqrs.bench.runner;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BencherTest {

	public static void main(String[] args) {
		checkImplementations();

		System.out.println("" //
				+ "-------------------------------------------------------\n"//
				+ "NB:\n"//
				+ "If your implementation was not found, please check that:\n" //
				+ " - you have placed at least one implementation in the classpath\n" //
				+ " - each implementation define a '/META-INF/services/" + CardService.class.getName() + "' file as per the ServiceLoader API specifications\n" //
				+ "-------------------------------------------------------");
		System.out.println("");

		for (CardService cs : ServiceLoader.load(CardService.class)) {
			cs.start();
			System.out.printf("\n---- %8s ------------------------------------------------------------\n", cs.getName());
			System.out.print("<< warmup");
			warmup(cs);
			System.out.println(" >>");
			round(cs, 1000, 1000);
			round(cs, 10000, 10000);
			round(cs, 100000, 100000);
			cs.stop();
		}
		close();

		System.exit(0);
	}

	private static void warmup(CardService cs) {
		// Planning operations
		final Operation[] creations = planCardCreations(10000);

		final Operation[] authorizations = planAuthorizations(10000, 10000);

		run(cs, creations, authorizations, cs instanceof Empty);
	}

	private static void round(CardService cs, int nbCards, int nbAuthorizations) {
		System.gc();

		// Planning operations
		final Operation[] creations = planCardCreations(nbCards);

		final Operation[] authorizations = planAuthorizations(nbAuthorizations, nbCards);

		run(cs, creations, authorizations, true);
	}

	private static void run(CardService cs, Operation[] creations, Operation[] authorizations, boolean track) {
		cs.clear();
		if (cs.parallel()) {
			ExecutorService es = Executors.newFixedThreadPool(4);
			if (track) start(cs.getName(), "card creation " + creations.length, cs instanceof Empty);
			runAsync(cs, es, creations);
			if (track) end();
			if (track) start(cs.getName(), "authorization " + authorizations.length, cs instanceof Empty);
			runAsync(cs, es, authorizations);
			if (track) end();
			List<Runnable> remains = es.shutdownNow();
			if (null == remains || !remains.isEmpty()) { throw new RuntimeException("should remain nothing in the ES ?!?"); }
		} else {
			if (track) start(cs.getName(), "card creation " + creations.length, cs instanceof Empty);
			runSync(cs, creations);
			if (track) end();
			if (track) start(cs.getName(), "authorization " + authorizations.length, cs instanceof Empty);
			runSync(cs, authorizations);
			if (track) end();
		}
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
		for (Future<?> f : res)
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
	}

	/*
	 * Time tracking
	 */
	private static final Map<String, Bucket>	BUCKETS	= new HashMap<>();

	private static long												INIT;
	private static String											TEST_NAME;
	private static String											TYPE;
	private static boolean										SKIP;

	private static void start(String testName, String type, boolean skip) {
		TEST_NAME = testName;
		TYPE = type;
		SKIP = skip;
		INIT = System.nanoTime();
	}

	private static void close() {
		System.out.println();
		System.out.println();
		for (Bucket b : BUCKETS.values())
			System.out.println(b);
		BUCKETS.clear();
		System.gc();
	}

	private static void reset() {
		BUCKETS.clear();
		System.gc();
	}

	private static void end() {
		float duration = (System.nanoTime() - INIT) / 1000000f;
		System.out.printf("%7s %-25s: %,15.2f ms\n", TEST_NAME, TYPE, duration);
		if (!SKIP) {
			Bucket bucket = BUCKETS.get(TYPE);
			if (null == bucket) BUCKETS.put(TYPE, bucket = new Bucket(TYPE));
			bucket.add(TEST_NAME, duration);
		}
	}

	/*
	 * Implementations checking
	 */
	private static void checkImplementations() {
		Map<String, List<CardService>> css = new HashMap<>();
		for (CardService cs : ServiceLoader.load(CardService.class)) {
			String name = cs.getName();
			List<CardService> list = css.get(name);
			if (null == list) css.put(name, list = new ArrayList<>());
			list.add(cs);
		}
		if (css.isEmpty()) {
			System.err.println("Found nothing to run... Please check that:\n" + //
					" - you have placed at least one implementation in the classpath\n" + //
					" - each implementation define a '/META-INF/services/" + CardService.class.getName() + "' file as per the ServiceLoader API specifications");
			System.exit(1);
		}
		boolean dups = false;
		Formatter frep = new Formatter();
		for (Map.Entry<String, List<CardService>> e : css.entrySet()) {
			if (e.getValue().size() != 1) {
				dups = true;
				frep.format(" %7s:\n", e.getKey());
				for (CardService cs : e.getValue()) {
					Class<? extends CardService> clazz = cs.getClass();
					frep.format("---/!\\--->%20s from %s\n", clazz.getSimpleName(), clazz.getPackage().getName());
				}
			} else {
				CardService cs = e.getValue().get(0);
				Class<? extends CardService> clazz = cs.getClass();
				frep.format(" %7s: %20s from %s\n", e.getKey(), clazz.getSimpleName(), clazz.getPackage().getName());
			}
		}
		String rep = frep.out().toString();
		frep.close();
		if (dups) {
			System.err.print("Several implementations share the same name:\n" + rep);
			System.exit(1);
		} else {
			System.out.println("Discovered:");
			System.out.println(rep);
		}
	}

	/*
	 * Operations building
	 */
	private static abstract class Operation implements Runnable {
		protected CardService	serv;

		void setCardService(CardService serv) {
			this.serv = serv;
		}

	}

	private static Operation[] planAuthorizations(final int nbAuthorizations, final int nbCards) {
		final Operation[] authorizations = new Operation[nbAuthorizations];
		for (int i = 0; i < authorizations.length; i++) {
			final int j = i;
			authorizations[i] = new Operation() {
				@Override
				public void run() {
					DoAuthorization op = new DoAuthorization("" + (j % nbCards), "0514", 1);
					String resp = null;
					try {
						resp = serv.handle(op);
					} catch (Throwable t) {
						throw new RuntimeException("An error has occured while handling '" + op + "'", t);
					}
					if (!"00".equals(resp)) throw new RuntimeException("Got '" + resp + "' instead of '00' while '" + op + "'");
				}
			};
		}
		return authorizations;
	}

	private static Operation[] planCardCreations(int nbCards) {
		final Operation[] creations = new Operation[nbCards];
		for (int i = 0; i < creations.length; i++) {
			final int j = i;
			creations[i] = new Operation() {
				@Override
				public void run() {
					CreateCard op = new CreateCard("" + j, "0514");
					String resp = null;
					try {
						resp = serv.handle(op);
					} catch (Throwable t) {
						throw new RuntimeException("An error has occured while handling '" + op + "'", t);
					}
					if (!"00".equals(resp)) throw new RuntimeException("Got '" + resp + "' instead of '00' while '" + op + "'");
				}
			};
		}
		return creations;
	}

}
