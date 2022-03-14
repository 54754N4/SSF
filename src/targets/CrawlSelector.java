package targets;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import browser.common.Browser;
import browser.common.Configurators;
import browser.common.Options;
import crawler.model.Context;
import crawler.model.Context.Strategy;
import crawler.model.WebCrawler;

public class CrawlSelector extends WebCrawler implements TargetSelector {
	private final Queue<String> queue;

	private CrawlSelector(Context<String> context, int maxDepth) {
		super(context, maxDepth);
		queue = new LinkedBlockingQueue<>();
	}

	@Override
	protected Browser createBrowser() {
		return new Browser(Configurators.firefox()
				.config(Options.FIREFOX::defaultSettings)
//				.config(Options.FIREFOX::debugging)
				.build());
	}
	
	@Override
	protected void onVisit(String uri) throws Exception {
		super.onVisit(uri);
		queue.offer(uri);
	}
	
	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public String next() {
		return queue.poll();
	}

	public static class Builder extends WebCrawler.Builder<CrawlSelector> {		
		public Builder(Strategy strategy) {
			super(strategy);
		}
		
		public Builder() {
			this(Strategy.BREADTH_FIRST);
		}
		
		@Override
		public CrawlSelector build() {
			return new CrawlSelector(getContext(), getMaxDepth());
		}
	}
}
