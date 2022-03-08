package crawler;

import crawler.model.Context;
import crawler.model.Context.Strategy;
import crawler.model.Crawler;
import crawler.model.MultiCrawler;

public class MultiFolderCrawler extends MultiCrawler<String> {

	public MultiFolderCrawler(Context<String> context, int maxDepth, int maxThreads) {
		super(context, maxDepth, maxThreads);
	}
	
	public MultiFolderCrawler(Context<String> context, int maxDepth) {
		this(context, maxDepth, DEFAULT_MAX_THREADS);
	}

	@Override
	protected Crawler<String> create(Context<String> context, int maxDepth) {
		return new FolderCrawler(context, maxDepth);
	}
	
	public static class Builder extends MultiCrawler.Builder<String, MultiFolderCrawler> {
		public Builder(Strategy strategy) {
			super(strategy);
		}
		
		public Builder() {
			this(Strategy.BREADTH_FIRST);
		}

		@Override
		public MultiFolderCrawler build() {
			return new MultiFolderCrawler(getContext(), getMaxDepth(), getMaxThreads());
		}
	}
}
