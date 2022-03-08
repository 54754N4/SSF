package crawler.model;

import browser.common.Browser;
import crawler.model.Context.Strategy;

public abstract class MultiWebCrawler extends MultiCrawler<String> {

	public MultiWebCrawler(Context<String> context, int maxDepth, int maxThreads) {
		super(context, maxDepth, maxThreads);
	}

	protected abstract Browser createBrowser();
	
	/**
	 * Wire every child's WebCrawler browser to the one 
	 * created by the MultiWebCrawler.
	 */
	@Override
	protected WebCrawler create(Context<String> context, int maxDepth) {
		return new WebCrawler(context, maxDepth) {
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
	
	/**
	 * We only apply a restriction to all crawler builders that depend on this class.
	 * Restriction: Built crawler needs to be of type MultiWebCrawler
	 */
	public static abstract class Builder<R extends MultiWebCrawler> extends MultiCrawler.Builder<String, R> {
		public Builder(Strategy strategy) {
			super(strategy);
		}
		
		public Builder() {
			this(Strategy.BREADTH_FIRST);
		}
	}
}
