package test;

import crawler.Crawler.Strategy;
import crawler.impl.FolderCrawler;
import crawler.impl.FolderCrawler.Builder;
import crawler.impl.MultiFolderCrawler;
import crawler.impl.PageRankWebCrawler;

public class TestCrawlers {
	public static void main(String[] args) throws Exception {
//		testFolderCrawler();
//		testMultiFolderCrawler();
		testPageRank();
	}
	
	public static void testFolderCrawler() throws Exception {
		FolderCrawler crawler = new Builder()
			.asContext(context -> context.push("D:\\Desktop"))
			.setMaxDepth(3)
			.setStrategy(Strategy.DEPTH_FIRST)
			.build();
		crawler.crawl();
	}
	
	public static void testMultiFolderCrawler() throws Exception {
		try (MultiFolderCrawler crawler = new MultiFolderCrawler.Builder()
				.setMaxThreads(5)
				.asContext(c -> c.push("D:\\Desktop"))
				.setMaxDepth(5)
				.build()) {
			crawler.crawl();
		}
	}
	
	public static void testPageRank() throws Exception {
		try (PageRankWebCrawler crawler = new PageRankWebCrawler.Builder()
				.setOptimisations(100)
				.asContext(c -> c.push(
						"http://www.runescape.com",
						"http://www.google.com"
					).blacklist(
						"*.guinnessworldrecords.*",
						"*.bytedance.com",
						"*.tiktok.com"
					))
				.setMaxDepth(2)
				.build()) {
			crawler.crawl();
		}
	}
}
