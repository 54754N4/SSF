package compute.element;

import java.util.function.Predicate;

@FunctionalInterface
public interface ResilientPredicate<I> extends ResilientAction<I, Boolean>, Predicate<I> {
	@Override
	default Boolean compute(I in) throws Exception {
		return test(in);
	}
}