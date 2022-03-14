package crawler;

import ads.common.Utils.Time;
import browser.common.Browser;
import browser.common.Configurators;
import browser.common.Options;
import crawler.model.Context;
import crawler.model.MultiWebCrawler;
import crawler.model.WebCrawler;
import crawler.model.Context.Strategy;

public class MultiPageRankCrawler extends MultiWebCrawler {
	private PageRank pageRank;
	private int optimisations;
	private long duration;
	
	public MultiPageRankCrawler(Context<String> context, int maxDepth, int maxThreads, int optimisations, PageRank pageRank) {
		super(context, maxDepth, maxThreads);
		this.optimisations = optimisations;
		this.pageRank = pageRank;
	}
	
	@Override
	protected Browser createBrowser() { 
		return new Browser(
				Configurators.firefox()
					.config(Options.FIREFOX::defaultSettings)
//					.config(Configurations.FIREFOX::debugging)
					.build()
			);
	}
	
	@Override
	protected WebCrawler create(Context<String> context, int maxDepth) {
		return new PageRankCrawler(context, maxDepth, optimisations, pageRank) {
			@Override
			protected Browser createBrowser() {
				return MultiPageRankCrawler.this.createBrowser();
			}
			
			/* We want the multi-threaded crawler's postCrawl 
			 * to do the page-ranking algorithm instead of each
			 * worker */
			@Override protected void postCrawl() {}
		};
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
		close();
	}
	
	public static class Builder extends MultiWebCrawler.Builder<MultiPageRankCrawler> {
		private int optimisations;
		private PageRank pageRank;
		
		public Builder(Strategy strategy) {
			super(strategy);
			optimisations = PageRankCrawler.DEFAULT_OPTIMISATIONS;
			pageRank = new PageRank();
		}
		
		public Builder() {
			this(Strategy.BREADTH_FIRST);
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
		public MultiPageRankCrawler build() {
			return new MultiPageRankCrawler(
				getContext(),
				getMaxDepth(),
				getMaxThreads(),
				optimisations,
				pageRank);
		}
		
	}
}
