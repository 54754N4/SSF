package crawler.impl;

import java.util.HashSet;
import java.util.List;

import ads.common.Utils.Time;
import browser.common.Browser;
import browser.common.Configurations;
import browser.common.Configurators;
import browser.common.Pipeline;
import crawler.CrawlContext;
import crawler.WebCrawler;

public class PageRankWebCrawler extends WebCrawler {
	private int optimisations;
	private PageRank pageRank;
	private long duration;
	
	public PageRankWebCrawler(CrawlContext<String> context, int maxDepth, Strategy strategy, int optimisations) {
		super(context, maxDepth, strategy);
		this.optimisations = optimisations;
		pageRank = new PageRank();
	}
	
	public PageRankWebCrawler(CrawlContext<String> context, int maxDepth, int optimisations) {
		this(context, maxDepth, Strategy.BREADTH_FIRST, optimisations);
	}
	
	@Override
	protected Browser createBrowser() { 
		return new Browser(
			Configurators.FIREFOX.createDriver(
				Pipeline.start(Configurations.FIREFOX::defaultSettings)
					.then(Configurations.firefox()::debugging)
			)
		); 
	}

	@Override
	protected List<String> crawlFrontier(String uri) throws Exception {
		List<String> urls = super.crawlFrontier(uri);
		for (String child : urls) {
			if (!child.trim().equals("")) {
				pageRank.connections().putIfAbsent(uri, new HashSet<>());	// ignore duplicate links with set
				pageRank.connections().get(uri).add(child);
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
		System.out.println("\n> Page ranks per URL :");
		pageRank.optimize(optimisations)
			.forEach((key, val) -> System.out.printf("%s -> %f%n", key.toString(), val));
		duration = System.currentTimeMillis() - duration;
		System.out.printf("Finished after %s%n", Time.fromMillis(duration));
	}
	
	public static void main(String[] args) throws Exception {
		CrawlContext<String> context = CrawlContext.<String>create()
			.push(
				"http://www.runescape.com",
				"http://www.google.com"
			).blacklist(
				"*.guinnessworldrecords.*",
				"*.bytedance.com",
				"*.tiktok.com"
			);
		int maxDepth = 2, optimisationCycles = 100;
		try (PageRankWebCrawler crawler = new PageRankWebCrawler(context, maxDepth, optimisationCycles)) {
			crawler.crawl();
		}
	}
}