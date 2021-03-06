package crawler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import crawler.model.Context.Match;
import crawler.model.Context.Strategy;

/**
 * A Crawler is a thread that can search, up to a specified depth, 
 * starting from the initially given URIs (e.g. given before calling 
 * the Crawler::crawl method). 
 * 
 * @param <Uri> - anything that can be retrieved by crawling 
 */
public abstract class Crawler<Uri> implements Callable<Void>, Loggeable {
	public static final int DEFAULT_MAX_DEPTH = 1;
	public static final String 
		PRE_PHASE = "preCrawl",
		CRAWL_PHASE = "crawl",
		POST_PHASE = "postCrawl";
	
	/* Attributes */
	protected final int maxDepth;
	protected final Context<Uri> context;
	protected final List<Action> preExecute, postExecute, preVisit, postVisit;
	
	public Crawler(Context<Uri> context, int maxDepth) {
		if (maxDepth < 0)
			throw new IllegalArgumentException("Max depth can only be strict positive integers");
		this.context = context;
		this.maxDepth = maxDepth;
		preExecute = new ArrayList<>();
		postExecute = new ArrayList<>();
		preVisit = new ArrayList<>();
		postVisit = new ArrayList<>();
	}
	
	/**
	 * Force implementing classes to define how to retrieve the current 
	 * crawl frontier. 
	 */
	protected abstract List<Uri> crawlFrontier(Uri uri) throws Exception;
	
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
	
	public final void executeStrategy() throws Exception {
		preExecute();
		while (!context.storage.isEmpty()) {
			preVisit();
			Match<Uri> element = context.storage().pop();
			if (!validMatch(element))
				continue;
			onVisit(element.getUri());
			int nextDepth = element.getDepth() + 1;
			if (nextDepth > maxDepth)
				continue;
			for (Uri child : crawlFrontier(element.getUri()))
				context.storage().push(Match.of(nextDepth, child));
			postVisit();
		}
		postExecute();
	}
	
	private final boolean validMatch(Match<Uri> match) {
		if (match == null)
			return false;
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
		return true;
	}
	
	/* Lifecycle hooks */
	
	protected void preCrawl() throws Exception {
		logln(
			"Starting crawl with %s of %d",
			context.getStrategy() == Strategy.DEPTH_FIRST ? "max depth" : "breadth",
			maxDepth);
	}
	
	protected void onVisit(Uri uri) throws Exception {
		logln("Visiting (%d/%d): %s", context.count(), context.storage().size(), uri);
		context.markVisited(uri);
		context.increment();
	}
	
	protected void postCrawl() throws Exception {
		logln("Finished crawling.");
	}
	
	/* Visit and Execute hooks */
	
	private final void preExecute() throws Exception {
		for (Action action : preExecute)
			action.run();
	}
	
	private final void postExecute() throws Exception {
		for (Action action : postExecute)
			action.run();
	}
	
	public void addPreExecute(Action runnable) {
		preExecute.add(runnable);
	}
	
	public void addPostExecute(Action runnable) {
		postExecute.add(runnable);
	}
	
	public void clearPreExecute() {
		preExecute.clear();
	}
	
	public void clearPostExecute() {
		postExecute.clear();
	}
	
	private final void preVisit() throws Exception {
		for (Action action : preVisit)
			action.run();
	}
	
	private final void postVisit() throws Exception {
		for (Action action : postVisit)
			action.run();
	}
	
	public void addPreVisit(Action runnable) {
		preVisit.add(runnable);
	}
	
	public void addPostVisit(Action runnable) {
		postVisit.add(runnable);
	}
	
	public void clearPreVisit() {
		preVisit.clear();
	}
	
	public void clearPostVisit() {
		postVisit.clear();
	}
	
	/* Builder class */
	
	public static abstract class Builder<Uri, R> {
		private Context<Uri> context;
		private int maxDepth;
		
		public Builder(Strategy strategy) {
			context = Context.create(strategy);
			maxDepth = DEFAULT_MAX_DEPTH;
		}
		
		public Builder() {
			this(Strategy.BREADTH_FIRST);
		}
		
		public Builder<Uri, R> asContext(Consumer<Context<Uri>> consumer) {
			consumer.accept(context);
			return this;
		}
		
		public Builder<Uri, R> setContext(Context<Uri> context) {
			this.context = context;
			return this;
		}
		
		public Context<Uri> getContext() {
			return context;
		}
		
		public Builder<Uri, R> setMaxDepth(int maxDepth) {
			this.maxDepth = maxDepth;
			return this;
		}
		
		public int getMaxDepth() {
			return maxDepth;
		}
		
		public abstract R build();
	}
}