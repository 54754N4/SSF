package test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

import crawler.FolderCrawler;
import crawler.FolderCrawler.Builder;
import crawler.MultiFolderCrawler;
import crawler.MultiPageRankCrawler;
import crawler.PageRankCrawler;
import crawler.model.Crawler.Strategy;

public class TestCrawlers {
	public static void main(String[] args) throws Exception {
//		testFolderCrawler();
//		testMultiFolderCrawler();
//		testPageRank();
//		testMultiPageRank();
		testDFSAndBFS();
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
				.setMaxDepth(1)
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
				.setMaxDepth(1)
				.build()) {
			crawler.crawl();
		}
	}
	
	public static void testDFSAndBFS() throws Exception {
		// Sample Tree
		Tree root, l1, l2, l3, l4, l5, l6, l7, l8, l9, l10, l11, l12, l13, l14;
		root = new Tree();
		l1 = new Tree(); l2 = new Tree(); l3 = new Tree(); l4 = new Tree();
		l5 = new Tree(); l6 = new Tree(); l7 = new Tree(); l8 = new Tree();
		l9 = new Tree(); l10 = new Tree(); l11 = new Tree(); l12 = new Tree();
		l13 = new Tree(); l14 = new Tree();
		root.left = l1; root.right = l2;
		l1.left = l3; l1.right = l4;
		l2.left = l5; l2.right = l6;
		l3.left = l7; l3.right = l8;
		l4.left = l9; l4.right = l10;
		l5.left = l11; l5.right = l12;
		l6.left = l13; l6.right = l14;
		Stack<Tree> stack = new Stack<>(); 		// DFS
		Queue<Tree> queue = new LinkedList<>(); // BFS
		StorageSearchAdapter<Tree> storage = new StorageSearchAdapter<>(stack);
		storage.push(root);
		while (!storage.isEmpty()) {
			Tree tree = storage.pop();
			System.out.print(tree+", ");	// visit method
			if (tree.left != null)
				storage.push(tree.left);
			if (tree.right != null)
				storage.push(tree.right);
		}
	}
}

class Tree {
    static int count = 0;
    int c;
    Tree left, right;
    
    public Tree() {
    	c = count++;
    }
    
    public String toString() {
    	return ""+c;
    }
}

class StorageSearchAdapter<V> {
	private Collection<V> collection;
	private Consumer<V> pusher;
	private Supplier<V> popper;
	
	public StorageSearchAdapter(Consumer<V> pusher, Supplier<V> popper, Collection<V> collection) {
		this.pusher = pusher;
		this.popper = popper;
		this.collection = collection;
	}
	
	public StorageSearchAdapter(Queue<V> queue) {
		this(queue::offer, queue::poll, queue);
	}
	
	public StorageSearchAdapter(Stack<V> stack) {
		this(stack::push, stack::pop, stack);
	}
	
	public boolean isEmpty() {
		return collection.isEmpty();
	}
	
	public StorageSearchAdapter<V> push(V e) {
		pusher.accept(e);
		return this;
	}
	
	public V pop() {
		return popper.get();
	}
}
