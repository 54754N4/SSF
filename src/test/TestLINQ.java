package test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import compute.Queryable;

@SuppressWarnings("unused")
public class TestLINQ {
	public static void main(String[] args) {
//		testIndexer();
		testCountCountains();
//		testAggregates();
//		testReverse();
//		testEnsembleOperators();
//		testGroupBy();
//		testGroupJoin();
//		testJoin();
	}
	
	public static void testIndexer() {
		Iterator<Integer> indexer = Queryable.indexer();
		Stream.generate(indexer::next)
			.limit(10)
			.forEach(System.out::println);
	}
	
	public static void testCountCountains() {
		System.out.println(Queryable.of("apple","mango","orange","passionfruit","grape")
				.contains("grape"));
		
		System.out.println(Queryable.of(1,2,3,4,5,6,7,8,9,10)
				.sequenceEquals(Arrays.asList(1,2,3,4,5,6,7,8,9,10)));
	}
	
	public static void testAggregates() {
		System.out.println(Queryable.of("apple","mango","orange","passionfruit","grape")
			.aggregate(
				"banana",
				(longest, next) -> next.length() > longest.length() ? next : longest,
				String::toUpperCase));
			
		System.out.println(Queryable.of(1,2,3,4,5,6,7,8,9,10)
			.aggregate(Integer.valueOf(0), (c, e) -> c + 1));
		
		System.out.println(Queryable.of(1,2,3,4,5,6,7,8,9,10).count());
	}
	
	public static void testReverse() {
		try (Queryable<Integer> query = new Queryable<>(Arrays.asList(1,2,3,4,5,6,7,8,9).stream())) {
			query.reverse()
				.forEach(System.out::println);
		}
	}
	
	public static void testEnsembleOperators() {
		// Test union
		Integer[] u1 = { 5, 3, 9, 7, 5, 9, 3, 7 },
			u2 = { 8, 3, 6, 4, 4, 9, 1, 0 };
		System.out.print("Union:");
		Queryable.of(u1).union(u2)
			.forEach(System.out::print);
		System.out.println();
		

		// Test intersection
		Integer[] i1 = { 44, 26, 92, 30, 71, 38 },
				i2 = { 39, 59, 83, 47, 26, 4, 30 };
		System.out.print("Intersection:");
		Queryable.of(i1).intersect(i2)
			.forEach(System.out::print);
		System.out.println();
	}
	
	public static void testGroupBy() {
		final class Pet {
			String name;
			double age;
			
			Pet(String name, double age) {
				this.name = name;
				this.age = age;
			}
		}
		
		final class PetGroup {
			int key;
			long count;
			double min, max;
			
			PetGroup(int key, long count, Double...vals) {
				this.key = key;
				this.count = count;
				this.min = min(vals);
				this.max = max(vals);
			}
			
			double min(Double...vals) {
				double min = Double.MAX_VALUE;
				for (double val : vals)
					if (val < min)
						min = val;
				return min;
			}
			
			double max(Double...vals) {
				double max = Double.MIN_VALUE;
				for (double val : vals)
					if (val > max)
						max = val;
				return max;
			}
		}
		
		// Create stupid list of data
		List<Pet> pets = Arrays.asList(
			new Pet("Barley", 8.3),
			new Pet("Boots", 4.9),
			new Pet("Whiskers", 1.5),
			new Pet("Daisy", 4.3));
		// Try group by query
		try (Queryable<Pet> query = Queryable.of(pets)) {
			query.groupBy(
					pet -> (int) Math.floor(pet.age),
					pet -> pet.age,
					(baseAge, ages) -> new PetGroup(
						baseAge,
						ages.size(),
						ages.toArray(new Double[0])))
				.forEachIndexed((i, pg) -> System.out.printf(
					"%d: Age group = %d | Count = %d | Min = %f | Max = %f%n",
					i,
					pg.key,
					pg.count,
					pg.min,
					pg.max));
		}
	}
	
	public static void testGroupJoin() {
		final class Person {
			String name;
			
			Person(String name) {
				this.name = name;
			}
		}
		final class Pet {
			String name;
			Person owner;
			
			Pet(String name, Person owner) {
				this.name = name;
				this.owner = owner;
			}
		}
		final class PetGroup {
			String owner;
			List<String> pets;
			
			public PetGroup(String owner, List<String> pets) {
				this.owner = owner;
				this.pets = pets;
			}
		}
				
		// Create stupid list of data
		Person magnus, terry, charlotte;
		List<Person> people = Arrays.asList(
			magnus = new Person("Hedlund, Magnus"),
			terry = new Person("Adams, Terry"),
			charlotte = new Person("Weiss, Charlotte"));
		List<Pet> pets = Arrays.asList(
			new Pet("Barley", terry),
			new Pet("Boots", terry),
			new Pet("Whiskers", charlotte),
			new Pet("Daisy", magnus));
		
		// Try group join query
		try (Queryable<Person> query = Queryable.of(people)) {
			query.groupJoin(pets,
					person -> person,
					pet -> pet.owner,
					(person, petCollection) -> new PetGroup(
						person.name,
						petCollection.stream().map(pet -> pet.name).collect(Collectors.toList())))
				.forEachIndexed((i, pg) -> System.out.printf(
					"%d: Owner = %s | Count = %d | Pets = %s%n",
					i,
					pg.owner,
					pg.pets.size(),
					Arrays.toString(pg.pets.toArray())));
		}
	}
	
	public static void testJoin() {
		final class Person {
			String name;
			
			Person(String name) {
				this.name = name;
			}
		}
		final class Pet {
			String name;
			Person owner;
			
			Pet(String name, Person owner) {
				this.name = name;
				this.owner = owner;
			}
		}
		final class PersonPetPair {
			String owner, pet;
			
			public PersonPetPair(String owner, String pet) {
				this.owner = owner;
				this.pet = pet;
			}
		}
				
		// Create stupid list of data
		Person magnus, terry, charlotte;
		List<Person> people = Arrays.asList(
			magnus = new Person("Hedlund, Magnus"),
			terry = new Person("Adams, Terry"),
			charlotte = new Person("Weiss, Charlotte"));
		List<Pet> pets = Arrays.asList(
			new Pet("Barley", terry),
			new Pet("Boots", terry),
			new Pet("Whiskers", charlotte),
			new Pet("Daisy", magnus));
		
		// Try group join query
		try (Queryable<Person> query = Queryable.of(people)) {
			query.join(pets,
					person -> person,
					pet -> pet.owner,
					(person, pet) -> new PersonPetPair(person.name, pet.name))
				.forEachIndexed((i, pair) -> System.out.printf(
					"%d: Owner = %s | Pet = %s%n",
					i,
					pair.owner,
					pair.pet));
		}
	}
}
