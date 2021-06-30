package crawler;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.openqa.selenium.By;

import browser.common.Browser;

public abstract class WebCrawler extends Crawler<String> {
	private static final long TIMEOUT = 5; 	// in seconds
	private Browser browser;
	
	public WebCrawler(int maxDepth, Strategy strategy) {
		super(maxDepth, strategy);
		this.browser = setupBrowser();
	}

	public WebCrawler(int maxDepth) {
		this(maxDepth, Strategy.BREADTH_FIRST);	// usually depth-first is bad for web crawling
	}
	
	protected abstract Browser createBrowser();
	
	private final Browser setupBrowser() {
		return createBrowser()
				.waitImplicitly(TIMEOUT);
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
	
	protected String time(long millis) {
		Date date = new Date(millis);
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		return formatter.format(date);
	} 
}