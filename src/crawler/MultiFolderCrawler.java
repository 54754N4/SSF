package crawler;

import crawler.model.Context;
import crawler.model.Crawler;
import crawler.model.MultiCrawler;
import crawler.model.Crawler.Strategy;

public class MultiFolderCrawler extends MultiCrawler<String> {

	public MultiFolderCrawler(Context<String> context, int maxDepth, Strategy strategy, int maxThreads) {
		super(context, maxDepth, maxThreads, strategy);
	}
	
	public MultiFolderCrawler(Context<String> context, int maxDepth, Strategy strategy) {
		this(context, maxDepth, strategy, DEFAULT_MAX_THREADS);
	}

	@Override
	protected Crawler<String> create(Context<String> context, int maxDepth, Strategy strategy) {
		return new FolderCrawler(context, maxDepth, strategy);
	}
	
	public static class Builder extends MultiCrawler.Builder<String, MultiFolderCrawler> {
		@Override
		public MultiFolderCrawler build() {
			return new MultiFolderCrawler(getContext(), getMaxDepth(), getStrategy(), getMaxThreads());
		}
	}
}
