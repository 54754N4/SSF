package crawler;

import browser.common.Browser;
import crawler.Crawler.Strategy;

public abstract class MultiWebCrawler extends MultiCrawler<String> {

	public MultiWebCrawler(CrawlContext<String> context, int maxDepth, int maxThreads, Strategy strategy) {
		super(context, maxDepth, maxThreads, strategy);
	}

	protected abstract Browser createBrowser();
	
	/**
	 * Wire every child's WebCrawler browser to the one 
	 * created by the MultiWebCrawler.
	 */
	@Override
	protected WebCrawler create(CrawlContext<String> context, int maxDepth, Strategy strategy) {
		return new WebCrawler(context, maxDepth, strategy) {
			@Override
			protected Browser createBrowser() {
				return MultiWebCrawler.this.createBrowser();
			}
		};
	}
	
	@Override
	public void close() {
		super.close();
		crawlers.stream()
			.map(WebCrawler.class::cast)
			.forEach(WebCrawler::close);
	}
	
	public static abstract class Builder<R> extends WebCrawler.Builder<R> {
		public Builder() {
			super();
		}
	}
}
