package test;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.openqa.selenium.By;

import crawler.model.Context.Strategy;
import targets.CrawlSelector;
import targets.MultiCrawlSelector;
import targets.NextSelector;
import targets.PresetSelector;
import targets.TargetSelector;

public class TestTargetSelectors {
	public static void main(String[] args) throws Exception {
//		Iterator<String> iterator = testPresetSelector();
//		Iterator<String> iterator = testCrawlSelector();
//		Iterator<String> iterator = testMultiCrawlSelector();
		Iterator<String> iterator = testNextSelector();
		iterator.forEachRemaining(System.out::println);
	}
	
	public static TargetSelector testPresetSelector() {
		return new PresetSelector("first url", "second url", "third url");
	}
	
	public static TargetSelector testCrawlSelector() throws Exception {
		CrawlSelector selector = new CrawlSelector.Builder(Strategy.BREADTH_FIRST)
				.asContext(c -> c.push("http://www.runescape.com"))
				.setMaxDepth(1)
				.build();
		selector.crawl();
		return selector;
	}
	
	public static TargetSelector testMultiCrawlSelector() throws InterruptedException, ExecutionException {
		MultiCrawlSelector selector = new MultiCrawlSelector.Builder()
				.setMaxThreads(5)
				.asContext(c -> c.push("http://www.runescape.com"))
				.setMaxDepth(2)
				.build();
		selector.crawl();
		return selector;
	}
	
	public static NextSelector testNextSelector() {
		return new NextSelector.Builder()
				.setInitialUrl("https://www.lodgis.com/en/paris,long-term-rentals/")
				.setChecker(browser -> {
					return browser.findElement(By.cssSelector(".page-item.page-item-pager")) != null;
				})
				.setNextSelector(browser -> {
					return browser.findElement(By.cssSelector(".page-item.page-item-pager"))
							.findElement(By.tagName("a"))
							.getAttribute("href");
				})
				.build();
	}
}