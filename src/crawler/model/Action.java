package crawler.model;

@FunctionalInterface
public interface Action {
	void run() throws Exception;
}