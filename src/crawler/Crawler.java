package crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import browser.common.Constants;

/**
 * A Crawler is a thread that can search, up to a specified depth, 
 * starting from the initially given URIs (e.g. given before calling 
 * the Crawler::crawl method). 
 * 
 * @param <Uri> - anything that can be retrieved by crawling 
 */
public abstract class Crawler<Uri> implements Callable<Void> {
	
	/* Constants */
	
	public static final int DEFAULT_MAX_DEPTH = 1;
	public static final Map<String, String> PATTERN_2_REGEX_RULES;
	public static final String 
		DOT = "\\.", DOT_ESCAPED = "\\\\.",
		ANY = "\\*", ANY_REGEX = ".*",
		ANY_CHAR = "\\?", ANY_CHAR_REGEX = ".";
	public static enum Strategy { BREADTH_FIRST, DEPTH_FIRST }
	
	static {
		PATTERN_2_REGEX_RULES = new ConcurrentHashMap<>();
		PATTERN_2_REGEX_RULES.put(DOT, DOT_ESCAPED);
		PATTERN_2_REGEX_RULES.put(ANY, ANY_REGEX);
		PATTERN_2_REGEX_RULES.put(ANY_CHAR, ANY_CHAR_REGEX);
	}
	
	/* Attributes */
	
	protected final int maxDepth;
	protected final Strategy strategy;
	protected final List<String> blacklist;
	protected final Set<Uri> visited;
	protected final List<Uri> uris;
	private Predicate<Uri> filter;
	
	// used internally 
	
	private AtomicInteger count;
	private ReadWriteLock lock;
	private Lock write, read;
	
	public Crawler() {
		this(DEFAULT_MAX_DEPTH);
	}
	
	public Crawler(int maxDepth) {
		this(maxDepth, Strategy.BREADTH_FIRST);
	}
	
	public Crawler(int maxDepth, Strategy strategy) {
		if (maxDepth < 0)
			throw new IllegalArgumentException("Max depth can only be strict positive integers");
		this.maxDepth = maxDepth;
		this.strategy = strategy;
		uris = new ArrayList<>();
		visited = new HashSet<>();
		blacklist = new ArrayList<>();
		filter = uri -> true;
		lock = new ReentrantReadWriteLock();
		write = lock.writeLock();
		read = lock.readLock();
	}
	
	/**
	 * Force implementing classes to define how to retrieve the current 
	 * crawl frontier. 
	 */
	protected abstract List<Uri> crawlFrontier(Uri uri) throws Exception;
	
	/* Lifecycle hooks */
	
	protected void preCrawl() throws Exception {
		if (Constants.DEBUG) 
			System.out.printf("Starting crawl with max depth of %d%n", maxDepth);
	}

	protected boolean wasVisited(Uri uri) {
		return synchronizedContains(visited, uri);	// by default check current set
	}
	
	protected void onVisit(Uri uri) throws Exception {
		if (Constants.DEBUG) 
			System.out.printf(
					"Visiting %d/%d: %s%n", 
					count.get(),
					uris.size(),  
					uri);
	}
	
	protected void postCrawl() throws Exception {
		if (Constants.DEBUG) 
			System.out.println("Finished crawling.");
	}

	/* Accessors */
	
	public int getMaxDepth() {
		return maxDepth;
	}

	public Set<Uri> getVisited() {
		return visited;
	}
	
	/* Thread code */
	
	@Override
	public Void call() throws Exception {
		preCrawl();
		switch (strategy) {
			case BREADTH_FIRST: breadthFirst(); break;
			case DEPTH_FIRST: depthFirst(); break;
			default: throw new IllegalArgumentException("Strategy not implemented or mapped yet.");
		}
		postCrawl();
		return null;
	}
	
	/* Adding uris */
	
	public Crawler<Uri> push(Uri uri) {
		if (!wasVisited(uri))	// prevent re-crawling same targets
			synchronizedWrite(uris, uri);
		return this;
	}
	
	public Crawler<Uri> push(Collection<Uri> uris) {
		for (Uri uri : uris)
			push(uri);
		return this;
	}
	
	/* Adding blacklist */
		
	public static String sanitize(String pattern) {
		for (Map.Entry<String, String> entry : PATTERN_2_REGEX_RULES.entrySet())
			pattern = pattern.replaceAll(entry.getKey(), entry.getValue());
		return pattern;
	} 
	
	public Crawler<Uri> blacklist(String pattern) {
		blacklist.add(sanitize(pattern));
		return this;
	}
	
	public Crawler<Uri> blacklist(Collection<String> patterns) {
		for (String pattern : patterns)
			blacklist(pattern);
		return this;
	}
	
	public Crawler<Uri> unblacklist(String pattern) {
		blacklist.remove(pattern);
		return this;
	}
	
	public Crawler<Uri> unblacklist(Collection<String> patterns) {
		for (String pattern : patterns)
			unblacklist(pattern);
		return this;
	}
	
	protected boolean isBlacklisted(Uri uri) {
		for (String regex : blacklist) 
			if (Pattern.compile(regex)
					.matcher(uri.toString())
					.find())
				return true;
		return false;
	}
	
	/* Uri filtering methods */
	
	public Crawler<Uri> filter(Predicate<Uri> predicate) {
		filter = predicate;
		return this;
	}
	
	public Crawler<Uri> and(Predicate<Uri> predicate) {
		filter = filter.and(predicate);
		return this;
	}
	
	public Crawler<Uri> or(Predicate<Uri> predicate) {
		filter = filter.or(predicate);
		return this;
	}
	
	/* Synchronized count handling */
	
	protected AtomicInteger resetCounter() {
		return count = new AtomicInteger(0);
	}
	
	protected int increment() {
		return count.getAndIncrement();
	}
	
	protected int incrementBy(int by) {
		return count.getAndAdd(by);
	}
	
	protected int decrement() {
		return count.getAndDecrement();
	}
	
	protected int decrementBy(int by) {
		return incrementBy(-by);
	}
	
	/* Synchronized read and write access */
	
	protected <T> Crawler<Uri> synchronizedWrite(Collection<T> collection, T element) {
		write.lock();
		try { 
			collection.add(element); 
		} finally { 
			write.unlock();
		}
		return this;
	}
	
	protected <T> boolean synchronizedContains(Collection<T> collection, T element) {
		read.lock();
		try { 
			return collection.contains(element);
		} finally { 
			read.unlock();
		}
	}
	
	protected <T> T synchronizedRead(List<T> collection, int i) {
		read.lock();
		try { 
			return collection.get(i);
		} finally { 
			read.unlock();
		}
	}

	/* Crawling strategies */
	
	protected void breadthFirst() throws Exception {
		int depth = 0;
		final List<Uri> found = new ArrayList<>();
		resetCounter();
		while (!uris.isEmpty() && depth++ < maxDepth) {
			System.out.printf("Scanning breadth %d%n", depth);
			// Scan current depth
			for (Uri uri : uris) {
				increment();
				if (isBlacklisted(uri)) {
					System.out.printf("Ignored: %s (blacklisted)%n", uri);
					continue;
				} else if (filter.test(uri) && !wasVisited(uri)) {
					synchronizedWrite(visited, uri);				// mark as visited
					onVisit(uri);
					found.addAll(crawlFrontier(uri));
				} else 
					System.out.printf("Ignored: %s (%s)%n", uri, filter.test(uri) ? "pre-visited" : "filtered");
			}
			// Update URIs of next depth
			decrementBy(uris.size());
			uris.clear();
			push(found);
			found.clear();
		}
	}
	
	protected void depthFirst() throws Exception {
		resetCounter();
		depthFirst(0, uris);
	}
	
	private void depthFirst(int depth, final List<Uri> crawlFrontier) throws Exception {
		if (depth >= maxDepth)
			return;
		System.out.printf("Scanning depth %d%n", depth);
		for (Uri uri : crawlFrontier) {
			increment();
			if (isBlacklisted(uri)) {
				System.out.printf("Ignored : %s (blacklisted)%n", uri);
				continue;
			} else if (filter.test(uri) && !wasVisited(uri)) {
				synchronizedWrite(visited, uri);				// mark as visited
				onVisit(uri);
				depthFirst(depth+1, crawlFrontier(uri));
			} else 
				System.out.printf("Ignored : %s (filtered|wasVisited)%n");
		}
		decrementBy(crawlFrontier.size());
	}
}
