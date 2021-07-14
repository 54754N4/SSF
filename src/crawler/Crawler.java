package crawler;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import crawler.CrawlContext.Match;

/**
 * A Crawler is a thread that can search, up to a specified depth, 
 * starting from the initially given URIs (e.g. given before calling 
 * the Crawler::crawl method). 
 * 
 * @param <Uri> - anything that can be retrieved by crawling 
 */
public abstract class Crawler<Uri> implements Callable<Void>, Loggeable {
	/* Constants */
	public static enum Strategy { BREADTH_FIRST, DEPTH_FIRST }
	public static final int DEFAULT_MAX_DEPTH = 1;
	public static final String 
		PRE_PHASE = "preCrawl",
		CRAWL_PHASE = "crawl",
		POST_PHASE = "postCrawl";

	/* Attributes */
	protected final int maxDepth;
	protected final Strategy strategy;
	protected final CrawlContext<Uri> context;
	
	public Crawler(CrawlContext<Uri> context, int maxDepth, Strategy strategy) {
		if (maxDepth < 0)
			throw new IllegalArgumentException("Max depth can only be strict positive integers");
		this.context = context;
		this.maxDepth = maxDepth;
		this.strategy = strategy;
	}
	
	/**
	 * Force implementing classes to define how to retrieve the current 
	 * crawl frontier. 
	 */
	protected abstract List<Uri> crawlFrontier(Uri uri) throws Exception;
	
	/* Lifecycle hooks */
	
	protected void preCrawl() throws Exception {
		logln(
			"Starting crawl with %s of %d",
			strategy == Strategy.DEPTH_FIRST ? "max depth" : "breadth",
			maxDepth);
	}
	
	protected void onVisit(Uri uri) throws Exception {}
	
	protected void postCrawl() throws Exception {
		logln("Finished crawling.");
	}
	
	/* Crawling methods */
	
	public Crawler<Uri> crawl() throws Exception {
		preCrawl();
		executeStrategy();
		postCrawl();
		return this;
	}
	
	public Crawler<Uri> safeCrawl(BiConsumer<String, Throwable> onError) {
		try { preCrawl(); } 
		catch (Exception e) { onError.accept(PRE_PHASE, e); }
		try { executeStrategy(); } 
		catch (Exception e) { onError.accept(CRAWL_PHASE, e); }
		try { postCrawl(); }
		catch (Exception e) { onError.accept(POST_PHASE, e); }
		return this;
	}
	
	@Override
	public Void call() throws Exception {
		crawl();
		return null;
	}
	
	private void executeStrategy() throws Exception {
		StrategyMethod<Uri> strategy = this.strategy == Strategy.BREADTH_FIRST ?
				this::breadthFirst :
				this::depthFirst;
		Match<Uri> match;
		while ((match = context.uris.poll()) != null)
			strategy.execute(match);
	}

	/* Crawling strategies */
	
	protected void breadthFirst(Match<Uri> match) throws Exception {
		if (!handleMatch(match))
			return;
		for (Uri child : crawlFrontier(match.getUri()))
			if (match.getDepth() < maxDepth)
				context.push(match.getDepth() + 1, child);
	}
	
	protected void depthFirst(Match<Uri> match) throws Exception {
		if (!handleMatch(match))
			return;
		depthFirstPush(match);
	}
	
	private void depthFirstPush(Match<Uri> match) throws Exception {
		if (match.getDepth() >= maxDepth)
			return;
		Iterator<Uri> iterator = crawlFrontier(match.getUri()).iterator();
		if (!iterator.hasNext())
			return;
		Match<Uri> firstChild = Match.of(match.getDepth() + 1, iterator.next());
		if (!handleMatch(firstChild))
			return;
		depthFirstPush(firstChild);
		iterator.forEachRemaining(child -> context.push(match.getDepth()+1, child));
	}
	
	protected boolean handleMatch(Match<Uri> match) throws Exception {
		Uri uri = match.getUri();
		boolean blacklisted = context.isBlacklisted(uri),
				visited = context.wasVisited(uri), 
				filtered = !context.isAllowed(uri);
		if (blacklisted || visited || filtered) {
			if (blacklisted)
				logln("Ignored: %s (blacklisted)", uri);
			else if (visited)
				logln("Ignored: %s (pre-visited)", uri);
			else if (filtered) 
				logln("Ignored: %s (filtered)", uri);
			return false;
		}
		logln("Visiting (%d/%d): %s", context.count(), context.uris.size(), uri);
		context.markVisited(uri);
		onVisit(uri);
		context.increment();
		return true;
	}
	
	@FunctionalInterface
	public static interface StrategyMethod<Uri> {
		void execute(Match<Uri> uri) throws Exception;
	} 
	
	public static abstract class Builder<Uri, R> {
		private CrawlContext<Uri> context;
		private int maxDepth;
		private Strategy strategy;
		
		public Builder() {
			context = CrawlContext.create();
			maxDepth = DEFAULT_MAX_DEPTH;
			strategy = Strategy.BREADTH_FIRST;
		}
		
		public Builder<Uri, R> asContext(Consumer<CrawlContext<Uri>> consumer) {
			consumer.accept(context);
			return this;
		}
		
		public Builder<Uri, R> setContext(CrawlContext<Uri> context) {
			this.context = context;
			return this;
		}
		
		public CrawlContext<Uri> getContext() {
			return context;
		}
		
		public Builder<Uri, R> setMaxDepth(int maxDepth) {
			this.maxDepth = maxDepth;
			return this;
		}
		
		public int getMaxDepth() {
			return maxDepth;
		}
		
		public Builder<Uri, R> setStrategy(Strategy strategy) {
			this.strategy = strategy;
			return this;
		}
		
		public Strategy getStrategy() {
			return strategy;
		}
		
		public abstract R build();
	}
}