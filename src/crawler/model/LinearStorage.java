package crawler.model;

import java.util.Collection;
import java.util.Queue;
import java.util.Stack;
import java.util.function.Consumer;
import java.util.function.Supplier;

/* Thread-safe linear storage to allow using a Queue (FIFO)
 * or Stack (LIFO) as backend to generically implement
 * Depth-first search and Breadth-first search.
 */
public class LinearStorage<V> {
	private Collection<V> collection;
	private Consumer<V> pusher;
	private Supplier<V> popper;
	
	private LinearStorage(Consumer<V> pusher, Supplier<V> popper, Collection<V> collection) {
		this.pusher = pusher;
		this.popper = popper;
		this.collection = collection;
	}
	
	public LinearStorage(Queue<V> queue) {
		this(queue::offer, queue::poll, queue);
	}
	
	public LinearStorage(Stack<V> stack) {
		this(stack::push, stack::pop, stack);
	}
	
	public synchronized boolean isEmpty() {
		return collection.isEmpty();
	}
	
	public synchronized int size() {
		return collection.size();
	}
	
	public synchronized LinearStorage<V> push(V e) {
		pusher.accept(e);
		return this;
	}
	
	public synchronized V pop() {
		return popper.get();
	}
}
