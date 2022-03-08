package crawler.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Context<Uri> {
	protected final Queue<Match<Uri>> uris;
	protected final Set<Uri> visited;
	protected final Set<Pattern> blacklist;
	private Predicate<Uri> filter;
	
	/* Internal */
	private final AtomicInteger count;
	
	public static final Comparator<Pattern> PATTERN_COMPARATOR = new Comparator<>() {
		@Override
		public int compare(Pattern o1, Pattern o2) {
			return o1.pattern().compareTo(o2.pattern());
		}
	};
	
	public Context(List<Uri> uris, Set<Uri> visited, List<String> blacklist, Predicate<Uri> filter, int count) {
		this.uris = new ConcurrentLinkedDeque<>(Match.of(uris));
		this.visited = new ConcurrentSkipListSet<>(visited);
		this.blacklist = new ConcurrentSkipListSet<>(PATTERN_COMPARATOR);
		this.count = new AtomicInteger(count);
		this.filter = filter;
		for (String url : blacklist)
			blacklist(url);
	}
	
	public Context() {
		this(new ArrayList<>(), new HashSet<>(), new ArrayList<>(), uri -> true, 0);
	}
	
	public static <T> Context<T> create() {
		return new Context<>();
	}
	
	/* Synchronised visits count handling */
	
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
	
	public Context<Uri> blacklist(String pattern) {
		blacklist.add(Pattern.compile(URLTranslator.translate(pattern)));
		return this;
	}
	
	public Context<Uri> unblacklist(String pattern) {
		String converted = URLTranslator.translate(pattern);
		Pattern found = null;
		for (Pattern item : blacklist)
			if (item.pattern().equals(converted))
				found = item;
		if (found != null)
			blacklist.remove(found);
		return this;
	}
	
	public Context<Uri> blacklist(Collection<String> patterns) {
		for (String pattern : patterns)
			blacklist(pattern);
		return this;
	}
	
	public Context<Uri> blacklist(String...patterns) {
		for (String pattern : patterns)
			blacklist(pattern);
		return this;
	}
	
	public Context<Uri> unblacklist(Collection<String> patterns) {
		for (String pattern : patterns)
			unblacklist(pattern);
		return this;
	}
	
	public Context<Uri> unblacklist(String...patterns) {
		for (String pattern : patterns)
			unblacklist(pattern);
		return this;
	}
	
	protected boolean isBlacklisted(Uri uri) {
		for (Pattern pattern : blacklist)
			if (pattern.matcher(uri.toString())
					.find())
				return true;
		return false;
	}
	
	/* Uri filtering methods */
	
	public Context<Uri> filter(Predicate<Uri> predicate) {
		filter = predicate;
		return this;
	}
	
	public Context<Uri> and(Predicate<Uri> predicate) {
		filter = filter.and(predicate);
		return this;
	}
	
	public Context<Uri> or(Predicate<Uri> predicate) {
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
	
	public Context<Uri> push(int depth, Uri uri) {
		if (!wasVisited(uri))	// prevent re-crawling same targets
			uris.offer(Match.of(depth, uri));
		return this;
	}
	
	public Context<Uri> push(int depth, Collection<Uri> uris) {
		for (Uri uri : uris)
			push(depth, uri);
		return this;
	}
	
	public Context<Uri> push(int depth, @SuppressWarnings("unchecked") Uri...uris) {
		for (Uri uri : uris)
			push(depth, uri);
		return this;
	}
	
	public Context<Uri> push(Uri uri) {
		return push(0, uri);
	}
	
	public Context<Uri> push(@SuppressWarnings("unchecked") Uri...uris) {
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