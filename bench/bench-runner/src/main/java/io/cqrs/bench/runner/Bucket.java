package io.cqrs.bench.runner;

import java.util.Formatter;
import java.util.SortedSet;
import java.util.TreeSet;

public class Bucket {

	private static final class Point implements Comparable<Point> {
		final String	name;
		final float		time;

		Point(String name, float duration) {
			this.name = name;
			this.time = duration;
		}

		@Override
		public int compareTo(Point o) {
			return o.time > time ? -1 : (o.time == time ? 0 : 1);
		}
	}

	private final SortedSet<Point>	content	= new TreeSet<>();
	private final String						type;

	Bucket(String type) {
		this.type = type;
	}

	void add(String name, float duration) {
		content.add(new Point(name, duration));
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("For %-25s ", type);
		if (content.isEmpty()) {
			f.format("No data");
		} else {
			float ref = -1;
			for (Point p : content) {
				f.format(p.name);
				if (-1 == ref)
					ref = content.first().time;
				else
					f.format(" (%,8.2fx)", p.time / ref);
				f.format("  <  ");
			}
			f.close();
			sb.delete(sb.length() - 3, sb.length());
		}
		return sb.toString();
	}
}
