package crawler;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import browser.common.Browser;
import browser.common.Configurations;
import browser.common.Configurators;
import browser.common.Pipeline;

public class PageRankWebCrawler extends WebCrawler {
	private int optimisations;
	private PageRank pageRank;
	private long duration;
	
	public PageRankWebCrawler(int maxDepth, Strategy strategy, int optimisations) {
		super(maxDepth, strategy);
		this.optimisations = optimisations;
		pageRank = new PageRank();
	}
	
	public PageRankWebCrawler(int maxDepth, int optimisations) {
		this(maxDepth, Strategy.BREADTH_FIRST, optimisations);
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
		System.out.printf("Finished after %s%n", time(duration));
	}
	
	public static void main(String[] args) throws ExecutionException, InterruptedException {
		Collection<String> urls = Arrays.asList(
			"http://www.runescape.com",
			"http://www.google.com"
		), blacklist = Arrays.asList(
			"*.guinnessworldrecords.*",
			"*.bytedance.com",
			"*.tiktok.com"
		);
		int maxDepth = 3, optimisationCycles = 100;
		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			PageRankWebCrawler crawler = new PageRankWebCrawler(maxDepth, optimisationCycles);
			crawler.push(urls).blacklist(blacklist);
			executor.submit(crawler).get();
		} finally {
			executor.shutdown();
		}
	}
}