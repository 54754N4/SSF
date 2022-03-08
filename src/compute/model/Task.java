package compute.model;

import java.util.Collection;

/* Creates an executable graph of tasks that can take
 * in an input of type T1 and return an element of 
 * type T2.
 */
public interface Task<T1, T2> {
	void connect(Task<T2, ?> next);
	Collection<Task<T2, ?>> getNextTasks();
	T2 execute(T1 in) throws Exception;
	
	default void preExecute() throws Exception {}
	default void postExecute() throws Exception {}
	default void onError(Throwable t) {}
	
	default void propagate(T2 in) throws Exception {
		for (Task<T2, ?> task : getNextTasks()) 
			task.run(in);
	}
	
	default void run(T1 in) {
		try {
			preExecute();
			T2 out = execute(in);
			postExecute();
			propagate(out);
		} catch (Exception e) {
			onError(e);
		}
	}
}