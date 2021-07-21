package crawler.model;

import java.io.Closeable;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import crawler.model.Crawler.Strategy;

public abstract class MultiCrawler<Uri> implements UncaughtExceptionHandler, Callable<Void>, Loggeable, Closeable {
	public static final int DEFAULT_MAX_THREADS = 5;
	public static final long DEFAULT_WORK_CHECK_DELAY = 5000;
	
	protected final int maxDepth;
	protected final Strategy strategy;
	protected final CrawlContext<Uri> context;
	protected final List<Crawler<Uri>> crawlers;
	
	private final int maxThreads;
	private final ExecutorService executor;
	private CountDownLatch latch;
	private AtomicInteger terminated;
	
	public MultiCrawler(CrawlContext<Uri> context, int maxDepth, int maxThreads, Strategy strategy) {
		this.maxDepth = maxDepth;
		this.strategy = strategy;
		this.context = context;
		this.maxThreads = maxThreads;
		crawlers = new ArrayList<>();
		executor = Executors.newFixedThreadPool(maxThreads+1);	// +1 to account for current crawler
	}
	
	protected abstract Crawler<Uri> create(CrawlContext<Uri> context, int maxDepth, Strategy strategy);
	
	@Override
	public void close() {
		executor.shutdown();
	}
	
	/* Accessors */
	
	public List<Crawler<Uri>> getCrawlers() {
		return crawlers;
	}
	
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
		latch = new CountDownLatch(maxThreads);
		logln("Starting mutli-threaded crawl with %s of %d", 
				strategy == Strategy.DEPTH_FIRST ? "max depth" : "breadth", 
				maxDepth);
	}
	
	protected void postCrawl() throws Exception {
		latch.await();
		logln("Finished multi-threaded crawl.");
	}
	
	@Override
	public Void call() throws Exception {
		List<Callable<Void>> callables = spawnThreads();
		preCrawl();
		executor.invokeAll(callables);
		postCrawl();
		return null;
	}
	
	private List<Callable<Void>> spawnThreads() throws InterruptedException {
		return Stream.generate(() -> context)
			.limit(maxThreads)					// duplicate context per threads
			.map(this::trackCrawlerCreation)
			.map(this::convertToWorker)
			.collect(Collectors.toList());
	}
	
	private final Crawler<Uri> trackCrawlerCreation(CrawlContext<Uri> context) {
		Crawler<Uri> crawler = create(context, maxDepth, strategy);
		crawlers.add(crawler);
		return crawler;
	}
	
	private final Callable<Void> convertToWorker(Crawler<Uri> crawler) {
		return new Callable<>() {
			@Override
			public Void call() throws Exception {
				// Keep crawling until no more work
				out: while (true) {
					// Call crawl method in worker thread's context
					try { crawler.call(); } 
					catch (Exception e) {
						logln("Killed thread that triggered error %s", this, e.getMessage());
						e.printStackTrace();
						latch.countDown();	// trip latch on error as well
						return null;
					}
					crawler.logln("Checking for extra tasks before stopping...", crawler);
					// Retry checking for new tasks (uses non-blocking sleep)
					long start = System.currentTimeMillis();
					while (context.uris.isEmpty()) 
						if (System.currentTimeMillis() - start >=  DEFAULT_WORK_CHECK_DELAY)
							break out;
				}
				// Worker thread has no more work and can terminate 
				latch.countDown();
				logln(
					"Thread terminated (%d/%d): %s",
					terminated.incrementAndGet(),
					maxThreads,
					crawler);
				return null;
			}
		};
	}
	
	public static abstract class Builder<Uri, R> extends Crawler.Builder<Uri, R> {
		private int maxThreads;
		
		public Builder() {
			super();
			maxThreads = DEFAULT_MAX_THREADS;
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