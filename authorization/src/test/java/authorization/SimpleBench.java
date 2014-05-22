package authorization;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SimpleBench {

	private static final int		NB_THREADS			= 20;
	protected static final int	NB_TRANSACTIONS	= 10000;

	@BeforeClass
	public static void prepare() {
	}

	@Test
	public void test1File() throws IOException {
		System.out.println("##### To file #####");
		System.out.println("To file\t: dry-run");
		doitFile();
		long time = System.currentTimeMillis();
		doitFile();
		System.out.println("To file\t: " + (System.currentTimeMillis() - time) + "ms");
	}

	@Test
	public void test2DbMultiThread() throws InterruptedException {
		System.out.println("##### To db [" + NB_THREADS + " threads] #####");
		ExecutorService pool = Executors.newFixedThreadPool(NB_THREADS);
		System.out.println("Multi-threaded\t: dry-run");
		doit(pool);
		long time = System.currentTimeMillis();
		doit(pool);
		System.out.println("Multi-threaded\t: " + (System.currentTimeMillis() - time) + "ms");

	}

	@Test
	public void test3DbMonoThread() throws InterruptedException {
		System.out.println("##### To db [monothread] #####");
		System.out.println("Mono-threaded\t: dry-run");
		doit(null);
		long time = System.currentTimeMillis();
		doit(null);
		System.out.println("Mono-threaded\t: " + (System.currentTimeMillis() - time) + "ms");
	}

	private void doit(ExecutorService pool) throws InterruptedException {
		if (null == pool) {
			new Task(null, NB_THREADS * NB_TRANSACTIONS).run();
		} else {
			CountDownLatch latch = new CountDownLatch(NB_THREADS);
			for (int i = 0; i < NB_THREADS; i++) {
				pool.submit(new Task(latch, NB_TRANSACTIONS));
			}
			latch.await();
		}
	}

	private static final class Task implements Runnable {

		private CountDownLatch	latch;
		private int							nb;

		public Task(CountDownLatch latch, int nb) {
			this.latch = latch;
			this.nb = nb;
		}

		@Override
		public void run() {
			try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/rm3?user=root&password=root")) {
				for (int i = 0; i < nb; i++) {
					conn.createStatement().execute("START TRANSACTION;");
					conn.createStatement().executeQuery("SELECT 1 FROM DUAL;");
					conn.createStatement().execute("COMMIT;");
				}
			} catch (SQLException ex) {
				ex.printStackTrace();
			} finally {
				if (null != latch)
					latch.countDown();
			}
		}

	}

	private void doitFile() throws IOException {
		Path file = Files.createTempFile("tmp", ".tmp");
		try (PrintWriter pw = new PrintWriter(new FileWriter(file.toFile(), true))) {
			for (int i = 0; i < NB_THREADS * NB_TRANSACTIONS; i++) {
				pw.print("Created event " + i + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
