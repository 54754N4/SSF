package crawler;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import ads.common.Utils.Time;
import browser.common.Browser;
import browser.common.Options;
import browser.common.Configurators;
import crawler.model.Context;
import crawler.model.WebCrawler;

public class PageRankCrawler extends WebCrawler {
	public static int DEFAULT_OPTIMISATIONS = 100;
	private int optimisations;
	private PageRank pageRank;
	private long duration;
	
	public PageRankCrawler(Context<String> context, int maxDepth, Strategy strategy, int optimisations, PageRank pageRank) {
		super(context, maxDepth, strategy);
		this.optimisations = optimisations;
		this.pageRank = pageRank;
	}
	
	@Override
	protected Browser createBrowser() { 
		return new Browser(
			Configurators.firefox()
				.config(Options.FIREFOX::defaultSettings)
//				.config(Options.FIREFOX::debugging)
				.build()
		);
	}
	@Override
	protected List<String> crawlFrontier(String uri) throws Exception {
		List<String> urls = super.crawlFrontier(uri);
		for (String child : urls) {
			if (!child.trim().equals("")) {
				pageRank.connections()
					.putIfAbsent(uri, new ConcurrentSkipListSet<>());	// ignore duplicate links with set
				pageRank.connections()
					.get(uri)
					.add(child);
			}
		}
		return urls;
	}
	
	@Override
	protected void preCrawl() throws Exception {
		super.preCrawl();
		duration = System.currentTimeMillis();
	}
	
	@Override
	protected void postCrawl() throws Exception {
		super.postCrawl();
		logln("> Page ranks per URL :");
		pageRank.optimize(optimisations)
			.forEach((key, val) -> logln("%s -> %f", key.toString(), val));
		duration = System.currentTimeMillis() - duration;
		logln("Finished after %s%n", Time.fromMillis(duration));
	}
	
	public static class Builder extends WebCrawler.Builder<PageRankCrawler> {
		private int optimisations;
		private PageRank pageRank;
		
		public Builder() {
			super();
			optimisations = DEFAULT_OPTIMISATIONS;
			pageRank = new PageRank();
		}
		
		public Builder setOptimisations(int optimisations) {
			this.optimisations = optimisations;
			return this;
		}
		
		public int getOptimisations() {
			return optimisations;
		}
		
		public Builder setPageRank(PageRank pageRank) {
			this.pageRank = pageRank;
			return this;
		}
		
		public PageRank getPageRank() {
			return pageRank;
		}
		
		@Override
		public PageRankCrawler build() {
			return new PageRankCrawler(
				getContext(),
				getMaxDepth(),
				getStrategy(),
				optimisations,
				pageRank);
		}
	}
}