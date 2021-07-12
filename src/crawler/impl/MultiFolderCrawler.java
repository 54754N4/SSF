package crawler.impl;

import java.util.concurrent.ExecutionException;

import ads.common.Utils;
import crawler.CrawlContext;
import crawler.Crawler;
import crawler.Crawler.Strategy;
import crawler.MultiCrawler;

public class MultiFolderCrawler extends MultiCrawler<String> {

	public MultiFolderCrawler(CrawlContext<String> context, int maxDepth, Strategy strategy, int maxThreads) {
		super(context, maxDepth, maxThreads, strategy);
	}
	
	public MultiFolderCrawler(CrawlContext<String> context, int maxDepth, Strategy strategy) {
		this(context, maxDepth, strategy, DEFAULT_MAX_THREADS);
	}

	@Override
	protected Crawler<String> create(CrawlContext<String> context, int maxDepth, Strategy strategy) {
		return new FolderCrawler(context, maxDepth, strategy);
	}
	
	public static class Builder extends MultiCrawler.Builder<String, MultiFolderCrawler> {
		@Override
		public MultiFolderCrawler build() {
			return new MultiFolderCrawler(getContext(), getMaxDepth(), getStrategy(), getMaxThreads());
		}
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		int maxDepth = 4;
		CrawlContext<String> context = CrawlContext.<String>create()
			.push("D:\\Desktop");
		try (MultiFolderCrawler crawler = new MultiFolderCrawler(context, maxDepth, Strategy.BREADTH_FIRST)) {
			long duration = System.currentTimeMillis();
			crawler.crawl();
			System.out.println(Utils.Time.fromMillis(System.currentTimeMillis() - duration));
		}
	}
}
