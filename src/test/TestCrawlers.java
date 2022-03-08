package test;

import crawler.FolderCrawler;
import crawler.FolderCrawler.Builder;
import crawler.model.Context.Strategy;
import crawler.MultiFolderCrawler;
import crawler.MultiPageRankCrawler;
import crawler.PageRankCrawler;

public class TestCrawlers {
	public static void main(String[] args) throws Exception {
//		testFolderCrawler();
//		testMultiFolderCrawler();
//		testPageRank();
		testMultiPageRank();
	}
	
	public static void testFolderCrawler() throws Exception {
		FolderCrawler crawler = new Builder(Strategy.DEPTH_FIRST)
			.asContext(context -> context.push("D:\\Desktop"))
			.setMaxDepth(3)
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
		try (PageRankCrawler crawler = new PageRankCrawler.Builder()
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
	
	public static void testMultiPageRank() throws Exception {
		try (MultiPageRankCrawler crawler = new MultiPageRankCrawler.Builder()
				.setOptimisations(100)
				.setMaxThreads(5)
				.asContext(c -> c.push(
						"http://www.runescape.com",
						"http://www.google.com"
					).blacklist(
						"*.guinnessworldrecords.*",
						"*.bytedance.com",
						"*.tiktok.com"
					))
				.setMaxDepth(3)
				.build()) {
			crawler.crawl();
		}
	}
}