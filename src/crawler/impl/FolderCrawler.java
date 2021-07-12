package crawler.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import crawler.CrawlContext;
import crawler.Crawler;

public class FolderCrawler extends Crawler<String> {
	// Set file walker max depth to 1 (since crawler has his own way of tracking depth anyways)
	private static final int WALKER_MAX_DEPTH = 1;
	
	public FolderCrawler(CrawlContext<String> context, int maxDepth, Strategy strategy) {
		super(context, maxDepth, strategy);
	}
	
	@Override
	protected List<String> crawlFrontier(String uri) throws IOException {
		return Files.walk(Paths.get(uri), WALKER_MAX_DEPTH)
			.map(Path::toFile)
			.filter(File::isDirectory)
			.filter(file -> !file.getAbsolutePath().equals(uri))
			.map(File::getAbsolutePath)
			.collect(Collectors.toList());
	}
	
	public static class Builder extends Crawler.Builder<String, FolderCrawler> {
		@Override
		public FolderCrawler build() {
			return new FolderCrawler(getContext(), getMaxDepth(), getStrategy());
		}
	}
	
	public static void main(String[] args) throws Exception {
		int maxDepth = 3;
		FolderCrawler crawler = new Builder()
				.asContext(context -> context.push("D:\\Desktop"))
				.setMaxDepth(maxDepth)
				.setStrategy(Strategy.DEPTH_FIRST)
				.build();
		crawler.crawl();
	}
}