package io.cqrs.bench.runner;

import io.cqrs.bench.api.CardService;
import io.cqrs.bench.api.CreateCard;
import io.cqrs.bench.api.DoAuthorization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class BencherTest {

	private static final int							PARALLELISM	= 4;

	private static final Random						RANDOM			= new Random(0);

	private static final ExecutorService	ES					= Executors.newFixedThreadPool(PARALLELISM);

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
			createCards(cs, 10000);
			doAuthors(cs, 10000, 1000000);
			cs.stop();
		}
		close();

		System.exit(0);
	}

	private static void warmup(CardService cs) {
		// Planning operations
		final Operation[] creations = planCardCreations(10000);
		run(cs, "warmup", creations, false);

		final Operation[] authorizations = planAuthorizations(10000, 10000);
		run(cs, "warmup", authorizations, false);
		System.out.flush();

		cs.clear();
	}

	private static void createCards(CardService cs, int nbCards) {

		// Planning operations
		Operation[] creations = planCardCreations(nbCards);

		System.gc();

		run(cs, "card creation", creations, true);
	}

	private static void doAuthors(CardService cs, int nbCards, int nbAuthorizations) {

		// Planning operations
		Operation[] authorizations = planAuthorizations(nbAuthorizations, nbCards);

		System.gc();

		run(cs, "authorization", authorizations, true);
	}

	private static void run(CardService cs, String type, Operation[] operations, boolean track) {
		int size = 0;
		{
			for (Operation op : operations)
				size += op.getSize();
		}
		if (cs.parallel()) {
			if (track) start(cs.getName(), type + " " + size, cs instanceof Empty);
			runAsync(cs, ES, operations);
			if (track) end();
		} else {
			if (track) start(cs.getName(), type + " " + size, cs instanceof Empty);
			runSync(cs, operations);
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

		abstract int getSize();

		void setCardService(CardService serv) {
			this.serv = serv;
		}

	}

	private static Operation[] planAuthorizations(final int nbAuthorizations, final int nbCards) {

		final int[] split = split(nbAuthorizations, PARALLELISM);
		final int[][] cards = new int[split.length][];
		{
			List<Integer> lcards = new ArrayList<>(nbAuthorizations);
			for (int i = 0; i < nbAuthorizations; i++) {
				lcards.add((i % nbCards));
			}
			Collections.shuffle(lcards, RANDOM);
			int next = 0;
			for (int i = 0; i < cards.length; i++) {
				cards[i] = new int[split[i]];
				for (int j = 0; j < cards[i].length; j++) {
					cards[i][j] = lcards.get(next++);
				}
			}
			lcards.clear();
			lcards = null;
		}

		final Operation[] authorizations = new Operation[cards.length];
		for (int i = 0; i < authorizations.length; i++) {
			final int[] work = cards[i];
			authorizations[i] = new Operation() {
				public void run() {
					for (int w : work) {
						DoAuthorization op = new DoAuthorization("" + w, "0514", 1);
						String resp = null;
						try {
							resp = serv.handle(op);
						} catch (Throwable t) {
							throw new RuntimeException("An error has occured while handling '" + op + "'", t);
						}
						if (!"00".equals(resp)) throw new RuntimeException("Got '" + resp + "' instead of '00' while '" + op + "'");
					}
				}

				int getSize() {
					return work.length;
				}
			};
		}

		return authorizations;
	}

	private static Operation[] planCardCreations(int nbCards) {
		final int[] split = split(nbCards, PARALLELISM);
		final int[][] cards = new int[split.length][];
		{
			int next = 0;
			for (int i = 0; i < cards.length; i++) {
				cards[i] = new int[split[i]];
				for (int j = 0; j < cards[i].length; j++) {
					cards[i][j] = next++;
				}
			}
		}

		final Operation[] creations = new Operation[cards.length];
		for (int i = 0; i < creations.length; i++) {
			final int[] work = cards[i];
			creations[i] = new Operation() {
				public void run() {
					for (int w : work) {
						CreateCard op = new CreateCard("" + w, "0514");
						String resp = null;
						try {
							resp = serv.handle(op);
						} catch (Throwable t) {
							throw new RuntimeException("An error has occured while handling '" + op + "'", t);
						}
						if (!"00".equals(resp)) throw new RuntimeException("Got '" + resp + "' instead of '00' while '" + op + "'");
					}
				}

				int getSize() {
					return work.length;
				}
			};
		}

		// final Operation[] creations = new Operation[cards.length];
		// for (int i = 0; i < creations.length; i++) {
		// final int j = i;
		// creations[i] = new Operation() {
		// @Override
		// public void run() {
		// CreateCard op = new CreateCard("" + j, "0514");
		// String resp = null;
		// try {
		// resp = serv.handle(op);
		// } catch (Throwable t) {
		// throw new RuntimeException("An error has occured while handling '" + op +
		// "'", t);
		// }
		// if (!"00".equals(resp)) throw new RuntimeException("Got '" + resp +
		// "' instead of '00' while '" + op + "'");
		// }
		// };
		// }
		return creations;
	}

	private static int[] split(int nbAuthorizations, int parallelism) {
		int[] split = new int[parallelism];
		if (nbAuthorizations <= parallelism) {
			for (int i = 0; i < split.length; i++) {
				split[i] = i < nbAuthorizations ? 1 : 0;
			}
		} else {
			for (int i = 0; i < split.length; i++) {
				split[i] = nbAuthorizations / parallelism;
			}
			split[0] += nbAuthorizations % parallelism;
		}
		return split;
	}

}
