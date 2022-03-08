package compute.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/* Allows the creation of some kind of multi-branched pipeline.
 * Each task only needs to know about it's current type.
 */
public abstract class DataFlow<T1, T2> implements Task<T1, T2> {
	private List<Task<T2, ?>> tasks;
	
	public DataFlow() {
		tasks = new ArrayList<>();
	}
	
	public void connect(Task<T2, ?> task) {
		tasks.add(task);
	}

	public Collection<Task<T2, ?>> getNextTasks() {
		return tasks;
	}
	
	public static <T1, T2> DataFlow<T1, T2> from(Function<T1, T2> function) {
		return new DataFlow<>() {
			@Override
			public T2 execute(T1 in) throws Exception {
				return function.apply(in);
			}
		};
	}
}
