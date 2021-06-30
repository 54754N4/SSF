package browser.common;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Pipeline<In, Out> extends Function<In, Out> {
	
	default <T> Pipeline<In, T> then(Pipeline<Out, T> nextStage) {
		return in -> nextStage.apply(apply(in));
	}
	
	// Convenience method to create pipes from a supplier method
	static <I> Pipeline<Void, I> start(Supplier<I> start) {
		return ignored -> start.get();
	}
	
	// Convenience method to create pipes from a reference
	static <I, O> Pipeline<I, O> of(Pipeline<I, O> start) {
		return in -> start.apply(in);
	}
	
	// Convenience method used only if In class type is Void
	default Out run() {
		return apply(null);
	}
	
	default Out run(In in) {
		return apply(in);
	}
	
	@SafeVarargs
	static <T> Consumer<T> pipe(Consumer<T>...stages) {
		if (stages.length == 0)
			return any -> {return;};
		Consumer<T> pipeline = stages[0];
		for (int i=1; i<stages.length; i++)
			pipeline = pipeline.andThen(stages[i]);
		return pipeline;
	}
	
	public static void main(String[] args) throws Exception {
		Pipeline<String, Integer> pipeline = Pipeline.of(String::trim)
				.then(String::isEmpty)
				.then(b -> b?1:0);
		System.out.println(pipeline.run("   "));
		System.out.println("test");
	}
}
