package targets;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import browser.common.Browser;
import browser.common.Configurators;
import browser.common.Options;
import crawler.model.Context;
import crawler.model.Context.Strategy;
import crawler.model.MultiWebCrawler;
import crawler.model.WebCrawler;

public class MultiCrawlSelector extends MultiWebCrawler implements TargetSelector  {
	private final Queue<String> queue;
	
	public MultiCrawlSelector(Context<String> context, int maxDepth, int maxThreads) {
		super(context, maxDepth, maxThreads);
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
	protected WebCrawler create(Context<String> context, int maxDepth) {
		return new WebCrawler(context, maxDepth) {
			@Override
			protected Browser createBrowser() {
				return MultiCrawlSelector.this.createBrowser();
			}
			
			@Override
			protected void onVisit(String uri) throws Exception {
				super.onVisit(uri);
				queue.offer(uri);
			}
		};
	}
	
	@Override
	public void postCrawl() throws Exception {
		super.postCrawl();
		close();
	}
	
	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public String next() {
		return queue.poll();
	}
	
	

	public static class Builder extends MultiWebCrawler.Builder<MultiCrawlSelector> {		
		public Builder(Strategy strategy) {
			super(strategy);
		}
		
		public Builder() {
			this(Strategy.BREADTH_FIRST);
		}
		
		@Override
		public MultiCrawlSelector build() {
			return new MultiCrawlSelector(getContext(), getMaxDepth(), getMaxThreads());
		}
	}
}
