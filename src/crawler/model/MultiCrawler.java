package crawler.model;

import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import crawler.model.Context.Strategy;

public abstract class MultiCrawler<Uri> implements UncaughtExceptionHandler, Callable<Void>, Loggeable, Closeable {
	public static final int DEFAULT_MAX_THREADS = 5;
	public static final long DEFAULT_WORK_CHECK_DELAY = 5000;
	
	protected final int maxDepth;
	protected final Context<Uri> context;
	
	private final int maxThreads;
	private final ExecutorService executor;
	private CountDownLatch startLatch, endLatch;
	private AtomicInteger terminated;
	private boolean first;
	
	public MultiCrawler(Context<Uri> context, int maxDepth, int maxThreads) {
		this.maxDepth = maxDepth;
		this.context = context;
		this.maxThreads = maxThreads;
		executor = Executors.newFixedThreadPool(maxThreads+1);	// +1 to account for current crawler
		first = true;
	}
	
	protected abstract Crawler<Uri> create(Context<Uri> context, int maxDepth);
	
	/* Threading methods */
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		logln("Thread %s triggered exception : %s", t, e);
	}
	
	public MultiCrawler<Uri> crawl() throws InterruptedException, ExecutionException {
		executor.submit(this).get();
		return this;
	}
	
	protected void preCrawl() throws Exception {
		terminated = new AtomicInteger();
		startLatch = new CountDownLatch(maxThreads);
		endLatch = new CountDownLatch(maxThreads);
		logln("Starting mutli-threaded crawl with %s of %d", 
				context.getStrategy() == Strategy.DEPTH_FIRST ? "max depth" : "breadth", 
				maxDepth);
	}
	
	protected void postCrawl() throws Exception {
		logln("Finished multi-threaded crawl.");
	}
	
	@Override
	public Void call() throws Exception {
		List<Callable<Void>> callables = spawnThreads();
		preCrawl();
		executor.invokeAll(callables);
		postCrawl();
		endLatch.await();
		return null;
	}
	
	private List<Callable<Void>> spawnThreads() throws InterruptedException {
		return Stream.generate(() -> create(context, maxDepth))
			.limit(maxThreads)
			.map(this::convertToWorker)
			.collect(Collectors.toList());
	}
	
	private final Callable<Void> convertToWorker(Crawler<Uri> crawler) {
		/* Forces one crawler/worker to gather enough tasks for 
		 * everyone before releasing all parked/awaiting workers.
		 */
		if (first) {
			crawler.addPreVisit(() -> startLatch.countDown());
			first = false;
		} else
			crawler.addPreExecute(() -> startLatch.await());
		return new Callable<>() {
			@Override
			public Void call() throws Exception {
				crawler.preCrawl();
				// Keep crawling until no more work
				out: while (true) {
					// Call crawl method in worker thread's context
					try { crawler.executeStrategy(); }
					catch (Exception e) {
						crawler.logln("Killed thread that triggered error %s", this, e.getMessage());
						e.printStackTrace();
						endLatch.countDown();	// trip latch on error as well
						return null;
					}
					// Retry checking for new tasks (using non-blocking sleep)
					crawler.logln("Checking for extra tasks before stopping...", crawler);
					long start = System.currentTimeMillis();
					while (context.storage.isEmpty()) 
						if (System.currentTimeMillis() - start >=  DEFAULT_WORK_CHECK_DELAY)
							break out;
				}
				// Worker thread has no more work and can terminate 
				endLatch.countDown();
				crawler.logln(
					"Thread terminated (%d/%d): %s",
					terminated.incrementAndGet(),
					maxThreads,
					crawler);
				crawler.postCrawl();
				return null;
			}
		};
	}
	
	@Override
	public void close() {
		executor.shutdown();
	}
	
	public static abstract class Builder<Uri, R> extends Crawler.Builder<Uri, R> {
		private int maxThreads;
		
		public Builder(Strategy strategy) {
			super(strategy);
			maxThreads = DEFAULT_MAX_THREADS;
		}
		
		public Builder() {
			this(Strategy.BREADTH_FIRST);
		}
		
		public Builder<Uri, R> setMaxThreads(int maxThreads) {
			this.maxThreads = maxThreads;
			return this;
		}
		
		public int getMaxThreads() {
			return maxThreads;
		}
	}
}