  package compute;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Stream-backed wrapper class. 
 * Allows to add functionality to streams.
 * 
 * @param <E> - element type of stream
 */
public abstract class BaseStream<E> implements Stream<E> {
	private Stream<E> stream;	// wrapper is backed by actual stream
	
	public BaseStream(Stream<E> stream) {
		setStream(stream);
	}

	protected Stream<E> setStream(Stream<E> stream) {
		return this.stream = stream;
	}
	
	/* Intermediate Operations */
	
	@Override
	public Iterator<E> iterator() {
		return stream.iterator();
	}

	@Override
	public Spliterator<E> spliterator() {
		return stream.spliterator();
	}

	@Override
	public boolean isParallel() {
		return stream.isParallel();
	}

	@Override
	public Stream<E> sequential() {
		return stream.sequential();
	}

	@Override
	public Stream<E> parallel() {
		return stream.parallel();
	}

	@Override
	public Stream<E> unordered() {
		return stream.unordered();
	}

	@Override
	public Stream<E> onClose(Runnable closeHandler) {
		return stream.onClose(closeHandler);
	}

	@Override
	public void close() {
		stream.close();
	}

	@Override
	public Stream<E> filter(Predicate<? super E> predicate) {
		return stream.filter(predicate);
	}

	@Override
	public <R> Stream<R> map(Function<? super E, ? extends R> mapper) {
		return stream.map(mapper);
	}

	@Override
	public IntStream mapToInt(ToIntFunction<? super E> mapper) {
		return stream.mapToInt(mapper);
	}

	@Override
	public LongStream mapToLong(ToLongFunction<? super E> mapper) {
		return stream.mapToLong(mapper);
	}

	@Override
	public DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper) {
		return stream.mapToDouble(mapper);
	}

	@Override
	public <R> Stream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper) {
		return stream.flatMap(mapper);
	}

	@Override
	public IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper) {
		return stream.flatMapToInt(mapper);
	}

	@Override
	public LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper) {
		return stream.flatMapToLong(mapper);
	}

	@Override
	public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper) {
		return stream.flatMapToDouble(mapper);
	}

	@Override
	public Stream<E> distinct() {
		return stream.distinct();
	}

	@Override
	public Stream<E> sorted() {
		return stream.sorted();
	}

	@Override
	public Stream<E> sorted(Comparator<? super E> comparator) {
		return stream.sorted(comparator);
	}

	@Override
	public Stream<E> peek(Consumer<? super E> action) {
		return stream.peek(action);
	}

	@Override
	public Stream<E> limit(long maxSize) {
		return stream.limit(maxSize);
	}

	@Override
	public Stream<E> skip(long n) {
		return stream.skip(n);
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		stream.forEach(action);
	}

	@Override
	public void forEachOrdered(Consumer<? super E> action) {
		stream.forEachOrdered(action);
	}

	/* Terminal Operations */
	
	@Override
	public Object[] toArray() {
		return stream.toArray();
	}

	@Override
	public <A> A[] toArray(IntFunction<A[]> generator) {
		return stream.toArray(generator);
	}

	@Override
	public E reduce(E identity, BinaryOperator<E> accumulator) {
		return stream.reduce(identity, accumulator);
	}

	@Override
	public Optional<E> reduce(BinaryOperator<E> accumulator) {
		return stream.reduce(accumulator);
	}

	@Override
	public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator, BinaryOperator<U> combiner) {
		return stream.reduce(identity, accumulator, combiner);
	}

	@Override
	public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator, BiConsumer<R, R> combiner) {
		return stream.collect(supplier, accumulator, combiner);
	}

	@Override
	public <R, A> R collect(Collector<? super E, A, R> collector) {
		return stream.collect(collector);
	}

	@Override
	public Optional<E> min(Comparator<? super E> comparator) {
		return stream.min(comparator);
	}

	@Override
	public Optional<E> max(Comparator<? super E> comparator) {
		return stream.max(comparator);
	}

	@Override
	public long count() {
		return stream.count();
	}

	@Override
	public boolean anyMatch(Predicate<? super E> predicate) {
		return stream.anyMatch(predicate);
	}

	@Override
	public boolean allMatch(Predicate<? super E> predicate) {
		return stream.allMatch(predicate);
	}

	@Override
	public boolean noneMatch(Predicate<? super E> predicate) {
		return stream.noneMatch(predicate);
	}

	@Override
	public Optional<E> findFirst() {
		return stream.findFirst();
	}

	@Override
	public Optional<E> findAny() {
		return stream.findAny();
	}
}