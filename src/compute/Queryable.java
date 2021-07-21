package compute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.google.common.base.Predicates;

/** 
 * Implements LINQ methods from C# 
 * @param <E> - type of elements  
 */
public class Queryable<E> extends BaseStream<E> {

	public Queryable(Stream<E> stream) {
		super(stream);
	}
	
	/* Filters */
	
	public boolean contains(E item) {
		return contains(item, E::equals);
	}
	
	public boolean contains(E item, BiFunction<E, E, Boolean> comparer) {
		return anyMatch(e -> comparer.apply(item, e));
	}
	
	public boolean sequenceEquals(Collection<E> collection) {
		return sequenceEquals(collection, (e1, e2) -> e1.equals(e2));
	}
	
	public boolean sequenceEquals(Collection<E> collection, BiFunction<E, E, Boolean> comparer) {
		Iterator<E> i1 = iterator(),
				i2 = collection.iterator();
		E e1, e2;
		while (i1.hasNext() && i2.hasNext()) {
			e1 = i1.next();
			e2 = i2.next();
			if (!comparer.apply(e1, e2))
				return false;
		}
		return !i1.hasNext() && !i2.hasNext();
	}

	public <A, R> R aggregate(
			A seed, 
			Predicate<E> predicate, 
			BiFunction<A, E, A> accumulator, 
			Function<A, R> selector) {
		Iterator<E> iterator = iterator();
		if (!iterator.hasNext())
			return selector.apply(seed);
		E element;
		A result = seed;
		while (iterator.hasNext()) {
			element = iterator.next();
			if (predicate.test(element))
				result = accumulator.apply(result, element);
		}
		return selector.apply(result);
	}
	
	public <A, R> R aggregate(A seed, BiFunction<A, E, A> accumulator, Function<A, R> selector) {
		return aggregate(seed, Predicates.alwaysTrue(), accumulator, selector);
	}
	
	public <A> A aggregate(A seed, BiFunction<A, E, A> accumulator) {
		return aggregate(seed, accumulator, x -> x);
	}

	@Override
	public long count() {
		return count(Predicates.alwaysTrue());
	}
	
	public long count(Predicate<E> predicate) {
		return aggregate(
			new AtomicLong(),
			(count, e) -> {
				if (predicate.test(e))
					count.incrementAndGet(); 
				return count; 
			},
			AtomicLong::get);
	}
	
	public Queryable<E> defaultIfEmpty(E defaultValue) { 
		return iterator().hasNext() ? this : new Queryable<>(Stream.of(defaultValue)); 
	}
	
	public <T> Queryable<T> cast(Function<E, T> caster) {
		return new Queryable<>(map(caster));
	}
	
	public Queryable<E> concat(E[] array) {
		return new Queryable<>(Stream.concat(this, Stream.of(array)));
	}
	
	public Queryable<E> concat(Collection<E> collection) {
		return new Queryable<>(Stream.concat(this, collection.stream()));
	}
	
	public Queryable<E> append(E element) {
		return new Queryable<>(Stream.concat(this, Stream.of(element)));
	}
	
	public Queryable<E> prepend(E element) {
		return new Queryable<>(Stream.concat(Stream.of(element), this));
	}
	
	public Queryable<E> except(Collection<E> collection) {
		Predicate<E> predicate = collection::contains;
		return new Queryable<>(filter(predicate.negate()));
	}
	
	public Queryable<E> except(E[] collection) {
		return except(Arrays.asList(collection));
	}
	
	public Queryable<E> intersect(Collection<E> collection) {
		return new Queryable<>(filter(collection::contains).distinct());
	}
	
	public Queryable<E> intersect(E[] collection) {
		return intersect(Arrays.asList(collection));
	}
	
	public Queryable<E> union(Collection<E> collection) {
		return new Queryable<>(Stream.concat(this, collection.stream()).distinct());
	}
	
	public Queryable<E> union(E[] collection) {
		return union(Arrays.asList(collection));
	}
	
	public <K, V, R> Queryable<R> groupBy(
			Function<E, K> keySelector,
			Function<E, V> valueSelector,
			BiFunction<K, List<V>, R> resultSelector) {
		final Map<K, List<V>> map = new ConcurrentHashMap<>();
		forEach(e -> {
			K key = keySelector.apply(e);
			V value = valueSelector.apply(e);
			map.putIfAbsent(key, new ArrayList<>());
			map.get(key).add(value);
		});
		Stream<R> result = map.entrySet()
			.stream()
			.map(e -> resultSelector.apply(e.getKey(), e.getValue()));
		return new Queryable<>(result);
	}
	
	public <K, I, R> Queryable<R> groupJoin(
			Collection<I> inner,
			Function<E, K> outerKeySelector,
			Function<I, K> innerKeySelector,
			BiFunction<E, List<I>, R> resultSelector) {
		final Map<K, E> elements = new ConcurrentHashMap<>();
		final Map<K, List<I>> map = new ConcurrentHashMap<>();
		forEach((E e) -> {
			map.put(outerKeySelector.apply(e), new ArrayList<>());
			elements.put(outerKeySelector.apply(e), e);
		});
		inner.forEach((I e) -> {
			List<I> list = map.get(innerKeySelector.apply(e));
			if (list != null)
				list.add(e);
		});
		Stream<R> result = map.entrySet()
			.stream()
			.map(e -> resultSelector.apply(elements.get(e.getKey()), e.getValue()));
		return new Queryable<>(result);
	}
	
	public <K, I, R> Queryable<R> join(
			Collection<I> inner,
			Function<E, K> outerKeySelector,
			Function<I, K> innerKeySelector,
			BiFunction<E, I, R> resultSelector,
			BiFunction<K, K, Boolean> comparer) {
		final List<Pair<E, I>> list = new ArrayList<>();
		forEach(o -> inner.forEach(i -> {
			K ok = outerKeySelector.apply(o),
				ik = innerKeySelector.apply(i);
			if (comparer.apply(ok, ik))
				list.add(new Pair<>(o, i));
		}));
		Stream<R> result = list.stream()
			.map(pair -> resultSelector.apply(pair.first, pair.second));
		return new Queryable<>(result);
	}
	
	public <K, I, R> Queryable<R> join(
			Collection<I> inner,
			Function<E, K> outerKeySelector,
			Function<I, K> innerKeySelector,
			BiFunction<E, I, R> resultSelector) {
		return join(
			inner,
			outerKeySelector,
			innerKeySelector,
			resultSelector,
			(e1, e2) -> e1.equals(e2));
	}
	
	public Queryable<E> reverse() {
		List<E> reversed = collect(asReversedList());
		return new Queryable<>(reversed.stream());
	}
	
	public Queryable<E> skipLast(long n) {
		return new Queryable<>(reverse().skip(n).collect(asReversedList()).stream());
	}
	
	public Queryable<E> takeLast(long n) {
		return new Queryable<>(reverse().limit(n).collect(asReversedList()).stream());
	}
	
	public Queryable<E> take(long n) {
		return new Queryable<>(limit(n));
	}
	
	/* Indexed alternatives of default Stream methods */
	
	public <R> Queryable<R> mapIndexed(BiFunction<Integer, E, R> indexedMapper) {
		final Iterator<Integer> indexer = indexer();
		return new Queryable<>(map(e -> indexedMapper.apply(indexer.next(), e)));
	}
	
	public void forEachIndexed(BiConsumer<Integer, E> indexedConsumer) {
		final Iterator<Integer> indexer = indexer();
		forEach(e -> indexedConsumer.accept(indexer.next(), e));
	}
	
	public Queryable<E> dropWhileIndexed(BiPredicate<Integer, E> condition) {
		final Iterator<Integer> indexer = indexer();
		return new Queryable<>(dropWhile(e -> condition.test(indexer.next(), e)));
	}
	
	public Queryable<E> takeWhileIndexed(BiPredicate<Integer, E> condition) {
		final Iterator<Integer> indexer = indexer();
		return new Queryable<>(takeWhile(e -> condition.test(indexer.next(), e)));
	}
	
	/* Conversions */
	
	public List<E> toList() {
		return collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
	}
	
	public Queryable<E> dup() {
		Supplier<Stream<E>> supplier = toList()::stream;
		setStream(supplier.get());
		return new Queryable<>(supplier.get());
	}
	
	/* Helper static methods */
	
	public static Iterator<Integer> indexer() {
		return Stream.iterate(0, i -> i+1).iterator();
	}
	
	public static <T> Queryable<T> of(Stream<T> stream) {
		return new Queryable<>(stream);
	}
	
	@SafeVarargs
	public static <T> Queryable<T> of(T...elements) {
		return of(Arrays.stream(elements));
	}

	public static <T> Queryable<T> of(final Collection<T> elements) {		
		return of(elements.stream());
	} 
	
	public static <E> Collector<E, List<E>, List<E>> asReversedList() {
		return Collector.of(
				ArrayList::new,
				(list, e) -> list.add(0, e),
				(l1, l2) -> { 
					l1.addAll(0, l2);
					return l1;
				});
	}
	
	/* Helper classes */
	
	public static class Pair<T1, T2> {
		public final T1 first;
		public final T2 second;
		
		public Pair(T1 first, T2 second) {
			this.first = first;
			this.second = second;
		}
	}
}