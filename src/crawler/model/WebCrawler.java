package crawler.model;

import java.io.Closeable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;

import browser.common.Browser;
import crawler.model.Context.Strategy;

public abstract class WebCrawler extends Crawler<String> implements Closeable {
	private Browser browser;
	
	public WebCrawler(Context<String> context, int maxDepth) {
		super(context, maxDepth);
	}
	
	protected abstract Browser createBrowser();
	
	@Override
	protected void preCrawl() throws Exception {
		if (browser == null)
			browser = createBrowser();
	}
	
	@Override
	protected void postCrawl() throws Exception {
		super.postCrawl();
		close();
	}
	
	@Override
	public void close() {
		if (browser != null) {
			browser.close();
			browser = null;
		}
	}
	
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
	
	public static abstract class Builder<R> extends Crawler.Builder<String, R> {
		public Builder(Strategy strategy) {
			super(strategy);
		}
		
		public Builder() {
			this(Strategy.BREADTH_FIRST);
		}
	}
}