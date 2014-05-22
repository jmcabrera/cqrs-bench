package authorization;

import static authorization.Bench.TestType.AUTHORIZATION;
import static authorization.Bench.TestType.CARD_CREATION;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import authorization.Bench.TestType;

public abstract class Bench {

	protected static final int	PARALLELISM	= 8;

	protected static enum TestType {
		CARD_CREATION(1000, "card creations"), //
		AUTHORIZATION(10000, "authorizations");

		public final int	nb;
		private String		name;

		TestType(int nb, String name) {
			this.nb = nb;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	static {
		System.out.printf("## Creating %,d cards\n", CARD_CREATION.nb);
		System.out.printf("## Issuing  %,d authorizations\n", AUTHORIZATION.nb);
		System.out.printf("## For JDBC ONLY: Using %,d threads\n", PARALLELISM);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				Bench.close();
			}
		}));
	}

	private static final Map<TestType, Bucket>	BUCKETS	= new HashMap<>();

	private long																init;
	private String															testName;
	private TestType														type;

	protected void start(String testName, TestType type) {
		this.testName = testName;
		this.type = type;
		this.init = System.nanoTime();
	}

	protected static void close() {
		System.out.println();
		System.out.println();
		for (Bucket b : BUCKETS.values()) {
			System.err.println(b);
		}
	}

	protected void end() {
		float duration = (System.nanoTime() - init) / 1000000f;
		System.out.printf("%s (%s) took\t: %,15.2f ms\n", testName, type, duration);
		Bucket bucket = BUCKETS.get(type);
		if (null == bucket) {
			bucket = new Bucket(type);
			BUCKETS.put(type, bucket);
		}
		bucket.add(testName, duration);
	}

}

final class Bucket {

	private static final class Point implements Comparable<Point> {
		public final String	name;
		public final float	time;

		public Point(String name, float duration) {
			this.name = name;
			this.time = duration;
		}

		@Override
		public int compareTo(Point o) {
			return o.time > time ? -1 : (o.time == time ? 0 : 1);
		}
	}

	private final SortedSet<Point>	content	= new TreeSet<>();
	private final TestType					type;

	Bucket(TestType type) {
		this.type = type;
	}

	void add(String name, float duration) {
		content.add(new Point(name, duration));
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("For ").append(type).append(":\t");
		if (content.isEmpty()) {
			sb.append("No data");
		} else {
			float ref = -1;
			for (Point p : content) {
				sb.append(p.name);
				if (-1 == ref)
					ref = content.first().time;
				else
					sb.append(" (").append(rel(p.time, ref)).append("x)");
				sb.append("  <  ");
			}
			sb.delete(sb.length() - 3, sb.length());
		}
		return sb.toString();
	}

	private String rel(float time, float ref) {
		try (Formatter f = new Formatter()) {
			return f.format("%,5.2f", (time / ref)).out().toString();
		}
	}
}
