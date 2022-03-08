package compute.element;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Has retry-ability built-in for extra resilience (retries
 * use exponential back-offs).
 *
 * @param <I> - input type that computation node will receive
 * @param <O> - output type that computation node will give/generate
 */
@FunctionalInterface
public interface ResilientAction<I, O> extends Function<I, O> {
	int DEFAULT_MAX_RETRIES = 3,
		DEFAULT_RETRY_BASE = 2,
		DEFAULT_INITIAL_DELAY = 2;
	
	/* Computation stage */
	
	O compute(I in) throws Exception;
	
	default O execute(I in) {
		int retries = 0,
			initial = retryInitialDelay(),
			max = maxRetries(),
			base = retryBase();
		O out = null;
		Throwable t = null;
		preExecute(in);
		while (retries < max) {
			try {
				out = compute(in);
				onSuccess(out);
				break;
			} catch (Exception e) {
				t = e;
				int delay = (int) Math.pow(base, retries + initial);
				try {
					System.out.printf("Retrying in %ds...%n", delay);
					TimeUnit.SECONDS.sleep(delay);
				} catch (InterruptedException ex) { 
					System.err.println(this+": Sleep was interrupted");
				}
				retries++;
			}
		}
		if (retries == max)
			onError(t);
		postExecute();
		return out;
	}
	
	/* Lifecycle hooks */
	
	default void preExecute(I in) { 
		System.out.printf("%s: Pre-execution stage with input %s%n", this, in);
	}
	
	default void postExecute() { 
		System.out.printf("%s: Post-execution stage%n", this);
	}
	
	default void onSuccess(O out) {
		System.out.printf("%s: Successful computation stage execution with output %s%n", this, out);
	}
	
	default void onError(Throwable t) {
		System.out.printf("%s: Computation stage triggered %s%n", this, t);
	}
	
	/* Resilience methods */
	
	default int maxRetries() {
		return DEFAULT_MAX_RETRIES;
	}
	
	default int retryBase() {		// retry delay with exponential back-off
		return DEFAULT_RETRY_BASE;
	}
	
	default int retryInitialDelay() {		// first retry delay: base^offset 
		return DEFAULT_INITIAL_DELAY;
	}
	
	/* Overriden method */
	
	@Override
	default O apply(I in) {
		return execute(in);
	}
	
	/* Wrapper */
	
	static <I, O> ResilientAction<I, O> wrap(Function<I, O> function) {
		return i -> function.apply(i);
	}
}