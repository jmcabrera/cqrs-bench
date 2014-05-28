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

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class BencherTest {

	private static final int				DEF_NB_CARDS_WARMUP	= 1000;
	private static final int				DEF_NB_AUTHS_WARMUP	= 10000;

	private static int							PARALLELISM					= 4;

	private static final Random			RANDOM							= new Random(0);

	private static ExecutorService	ES;

	public static void main(String[] args) {

		@SuppressWarnings("static-access")
		Option p = OptionBuilder//
				.withArgName("nbThreads")//
				.hasArg().isRequired()//
				.withDescription("the number of threads to run on for parallel implementations")//
				.create('t');

		@SuppressWarnings("static-access")
		Option c = OptionBuilder//
				.withArgName("nbCards")//
				.hasArg().isRequired()//
				.withDescription("the number of cards to create")//
				.create('c');

		@SuppressWarnings("static-access")
		Option a = OptionBuilder//
				.withArgName("nbAuths")//
				.hasArg().isRequired()//
				.withDescription("the number of authorizations to issue\nideally, a > c")//
				.create('a');
		@SuppressWarnings("static-access")
		Option d = OptionBuilder//
				.withArgName("nbCardsWarmup")//
				.hasArg().isRequired(false)//
				.withDescription("the number of cards to create during warmup.\nDefault is " + DEF_NB_CARDS_WARMUP)//
				.create('d');

		@SuppressWarnings("static-access")
		Option b = OptionBuilder//
				.withArgName("nbAuthsWarmup")//
				.hasArg().isRequired(false)//
				.withDescription("the number of authorizations to issue during wamup.\nDefault is " + DEF_NB_AUTHS_WARMUP)//
				.create('b');
		Options options = new Options();
		options.addOption(p);
		options.addOption(c);
		options.addOption(a);
		options.addOption(d);
		options.addOption(b);

		CommandLine line = null;
		int nbCards = 0;
		int nbAuths = 0;
		int wNbCards = 0;
		int wNbAuths = 0;
		try {
			line = new BasicParser().parse(options, args);
			PARALLELISM = Integer.parseInt(line.getOptionValue(p.getOpt()));
			nbCards = Integer.parseInt(line.getOptionValue(c.getOpt()));
			nbAuths = Integer.parseInt(line.getOptionValue(a.getOpt()));
			wNbCards = Integer.parseInt(line.getOptionValue(d.getOpt(), "" + DEF_NB_CARDS_WARMUP));
			wNbAuths = Integer.parseInt(line.getOptionValue(b.getOpt(), "" + DEF_NB_AUTHS_WARMUP));
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("bencher", options);
			System.exit(1);
		} catch (NumberFormatException e) {
			System.out.println("Cannot read a number " + e.getMessage());
			HelpFormatter hf = new HelpFormatter();
			hf.printHelp("bencher", options);
			System.exit(1);
		}
		ES = Executors.newFixedThreadPool(PARALLELISM);
		System.out.printf("Running with %d cards, %s authorizations and %s threads\n", nbCards, nbAuths, PARALLELISM);

		checkImplementations();

		System.out.println("" //
				+ "-------------------------------------------------------\n"//
				+ "NB:\n"//
				+ "If your implementation was not found, please check that:\n" //
				+ " - you have placed at least one implementation in the classpath\n" //
				+ " - each implementation define a '/META-INF/services/" + CardService.class.getName() + "' file as per the ServiceLoader API specifications\n" //
				+ "-------------------------------------------------------");
		System.out.println("");

		Operation[] cards = planCardCreations(nbCards);
		Operation[] auths = planAuthorizations(nbAuths, nbCards);
		for (CardService cs : ServiceLoader.load(CardService.class)) {
			System.out.println("gc");
			System.gc();
			cs.start();
			System.out.printf("\n---- %8s ------------------------------------------------------------\n", cs.getName());
			System.out.print("<< warmup");
			warmup(cs, wNbCards, wNbAuths);
			System.out.println(" >>");
			run(cs, "card creation", cards, true);
			run(cs, "authorization", auths, true);
			cs.stop();
		}
		close();

		System.exit(0);
	}

	private static void warmup(CardService cs, int wNbCards, int wNbAuths) {
		// Planning operations
		final Operation[] creations = planCardCreations(wNbCards);
		run(cs, "warmup", creations, false);

		final Operation[] authorizations = planAuthorizations(wNbAuths, wNbCards);
		run(cs, "warmup", authorizations, false);

		cs.clear();
	}

	private static void run(CardService cs, String type, Operation[] operations, boolean track) {
		int size = 0;
		for (Operation op : operations)
			size += op.getSize();

		if (cs.parallel()) {
			if (track) start(cs.getName(), type + " " + size, cs instanceof Empty);
			runAsync(cs, operations);
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

	private static void runAsync(CardService cs, Operation[] operations) {
		Future<?>[] res = new Future<?>[operations.length];
		for (int i = 0; i < operations.length; i++) {
			operations[i].setCardService(cs);
			res[i] = ES.submit(operations[i]);
		}
		for (Future<?> f : res)
			try {
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
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

	private static abstract class Operation implements Runnable {
		protected CardService	serv;

		abstract int getSize();

		void setCardService(CardService serv) {
			this.serv = serv;
		}

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

}
