package crawler.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CrawlContext<Uri> {
	/* Pattern matching/filtering */
	public static final String 
		DOT = "\\.", DOT_ESCAPED = "\\\\.",
		ANY = "\\*", ANY_REGEX = ".*",
		ANY_CHAR = "\\?", ANY_CHAR_REGEX = ".";
	public static final Map<String, String> PATTERN_2_REGEX_RULES;
	static {
		PATTERN_2_REGEX_RULES = new ConcurrentHashMap<>();
		PATTERN_2_REGEX_RULES.put(DOT, DOT_ESCAPED);
		PATTERN_2_REGEX_RULES.put(ANY, ANY_REGEX);
		PATTERN_2_REGEX_RULES.put(ANY_CHAR, ANY_CHAR_REGEX);
	}
	
	/* Attributes */
	protected final Set<String> blacklist;
	protected final Queue<Match<Uri>> uris;
	protected final Set<Uri> visited;
	private Predicate<Uri> filter;
	
	/* Internal */ 
	private final AtomicInteger count;
	
	public CrawlContext(List<Uri> uris, Set<Uri> visited, List<String> blacklist, Predicate<Uri> filter, int count) {
		this.uris = new ConcurrentLinkedDeque<>(Match.of(uris));
		this.visited = new ConcurrentSkipListSet<>(visited);
		this.blacklist = new ConcurrentSkipListSet<>(blacklist);
		this.count = new AtomicInteger(count);
		this.filter = filter;
	}
	
	public CrawlContext() {
		this(new ArrayList<>(), new HashSet<>(), new ArrayList<>(), uri -> true, 0);
	}
	
	public static <T> CrawlContext<T> create() {
		return new CrawlContext<>();
	}
	
	/* Synchronized count handling */
	
	protected void resetCounter() {
		count.set(0);
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
	
	public int count() {
		return count.get();
	}
	
	/* Adding blacklist */
		
	public static String sanitize(String pattern) {
		for (Map.Entry<String, String> entry : PATTERN_2_REGEX_RULES.entrySet())
			pattern = pattern.replaceAll(entry.getKey(), entry.getValue());
		return pattern;
	} 
	
	public CrawlContext<Uri> blacklist(String pattern) {
		blacklist.add(sanitize(pattern));
		return this;
	}
	
	public CrawlContext<Uri> blacklist(Collection<String> patterns) {
		for (String pattern : patterns)
			blacklist(pattern);
		return this;
	}
	
	public CrawlContext<Uri> blacklist(String...patterns) {
		for (String pattern : patterns)
			blacklist(pattern);
		return this;
	}
	
	public CrawlContext<Uri> unblacklist(String pattern) {
		blacklist.remove(pattern);
		return this;
	}
	
	public CrawlContext<Uri> unblacklist(Collection<String> patterns) {
		for (String pattern : patterns)
			unblacklist(pattern);
		return this;
	}
	
	public CrawlContext<Uri> unblacklist(String...patterns) {
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
	
	public CrawlContext<Uri> filter(Predicate<Uri> predicate) {
		filter = predicate;
		return this;
	}
	
	public CrawlContext<Uri> and(Predicate<Uri> predicate) {
		filter = filter.and(predicate);
		return this;
	}
	
	public CrawlContext<Uri> or(Predicate<Uri> predicate) {
		filter = filter.or(predicate);
		return this;
	}
	
	public boolean isAllowed(Uri uri) {
		return filter.test(uri);
	}
	
	/* Accessor and helper methods */
	
	public Set<Uri> getVisited() {
		return visited;
	}
	
	public boolean wasVisited(Uri uri) {
		return visited.contains(uri);
	}
	
	public void markVisited(Uri uri) {
		visited.add(uri);
	}
		
	/* Adding uris */
	
	public CrawlContext<Uri> push(int depth, Uri uri) {
		if (!wasVisited(uri))	// prevent re-crawling same targets
			uris.offer(Match.of(depth, uri));
		return this;
	}
	
	public CrawlContext<Uri> push(int depth, Collection<Uri> uris) {
		for (Uri uri : uris)
			push(depth, uri);
		return this;
	}
	
	public CrawlContext<Uri> push(int depth, @SuppressWarnings("unchecked") Uri...uris) {
		for (Uri uri : uris)
			push(depth, uri);
		return this;
	}
	
	public CrawlContext<Uri> push(Uri uri) {
		return push(0, uri);
	}
	
	public CrawlContext<Uri> push(@SuppressWarnings("unchecked") Uri...uris) {
		for (Uri uri : uris)
			push(uri);
		return this;
	}
	
	public static class Match<Uri> {
		private final int depth;
		private final Uri uri;
		
		private Match(int depth, Uri uri) {
			this.depth = depth;
			this.uri = uri;
		}
		
		public int getDepth() {
			return depth;
		}

		public Uri getUri() {
			return uri;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + depth;
			result = prime * result + ((uri == null) ? 0 : uri.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Match<?> other = (Match<?>) obj;
			if (depth != other.depth)
				return false;
			if (uri == null) {
				if (other.uri != null)
					return false;
			} else if (!uri.equals(other.uri))
				return false;
			return true;
		}
		
		public static <Uri> Match<Uri> of(int depth, Uri uri) {
			return new Match<>(depth, uri);
		}
		
		public static <Uri> Match<Uri> of(Uri uri) {
			return of(0, uri);
		}
		
		public static <Uri> List<Match<Uri>> of(List<Uri> list) {
			return list.stream()
					.map(Match::of)
					.collect(Collectors.toList());
		}
	}
}