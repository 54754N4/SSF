package crawler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FolderCrawler extends Crawler<String> {
	// Set file walker max depth to 1 (since crawler has his own way of tracking depth anyways)
	private static final int WALKER_MAX_DEPTH = 1;
	
	public FolderCrawler(int maxDepth, Strategy strategy) {
		super(maxDepth, strategy);
	}
	
	public FolderCrawler(int maxDepth) {
		super(maxDepth);
	}

	@Override
	protected List<String> crawlFrontier(String uri) throws IOException {
		return Files.walk(Paths.get(uri), WALKER_MAX_DEPTH)
			.map(Path::toFile)
			.filter(File::isDirectory)
			.map(File::getAbsolutePath)
			.collect(Collectors.toList());
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		FolderCrawler crawler = new FolderCrawler(3, Strategy.BREADTH_FIRST);
		crawler.push("D:\\Desktop\\Hacking\\RE\\CrackMes\\GuiGui");
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.submit(crawler).get();
		executor.shutdown();
	}
}