package crawler;

import java.io.Closeable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openqa.selenium.By;

import browser.common.Browser;

public abstract class WebCrawler extends Crawler<String> implements Closeable {
	private Browser browser;
	
	public WebCrawler(CrawlContext<String> context, int maxDepth, Strategy strategy) {
		super(context, maxDepth, strategy);
		this.browser = createBrowser();
	}

	public WebCrawler(CrawlContext<String> context, int maxDepth) {
		this(context, maxDepth, Strategy.BREADTH_FIRST);	// usually depth-first is bad for web crawling
	}
	
	protected abstract Browser createBrowser();
	
	@Override
	protected List<String> crawlFrontier(String uri) throws Exception {
		try { 
			return browser.visit(uri)
				.findElements(By.tagName("a"))		// get all <a> tags
				.stream()
				.map(e -> {
					try { return e.getAttribute("href"); }
					catch (Exception ex) { return null; }
				})	// get their url/links
				.map(url -> { 						// convert to URL objects
					try { return new URL(url);} 
					catch (Exception ex) { return null; }
				})
				.filter(url -> url != null && !url.getHost().equals(""))	// remove nulls
				.map(URL::getHost)					// convert to String only containing host part
				.map(url -> String.format("http://%s", url))	// prepend http protocol
				.distinct()
				.collect(Collectors.toList());
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}
	
	@Override
	public void close() {
		if (browser != null) 
			browser.close();
	}
	
	public static abstract class Builder<R> extends Crawler.Builder<String, R> {
		private Supplier<Browser> browserSupplier;
		
		public Builder() {
			super();
		}
		
		public Builder<R> setBrowserSupplier(Supplier<Browser> browserSupplier) {
			this.browserSupplier = browserSupplier;
			return this;
		}
		
		public Supplier<Browser> getBrowserSupplier() {
			return browserSupplier;
		}
	}
}