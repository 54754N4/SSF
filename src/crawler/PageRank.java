package crawler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ads.Matrix;
import ads.common.Maps;

public class PageRank {
	private final Map<String, Collection<String>> connections;
	
	// Used internally
	private final float d = 0.85f; 			// damping factor
	private List<String> websites;
	private Map<String, Integer> cache;		// index cache
	
	private Map<String, Double> lastResult;
	
	public PageRank() {
		connections = new ConcurrentHashMap<>();
	}
	
	public Map<String, Collection<String>> connections() {
		return connections;
	}
	
	public Map<String, Double> getLastResult() {
		return lastResult;
	}
	
	/**
	 * Initially all websites have the same page rank (e.g. probability)
	 * @return the initial page rank column vector
	 */
	protected Matrix createInitialProbabilityVector() {
		int size = connections.size();
		double[][] vector = new double[size][];
		for (int i=0; i<size; i++)
			vector[i] = new double[]{ 1d/size };
		return new Matrix(vector);
	}
	
	/**
	 * Square stochastic matrix where each column represents the probability
	 * of clicking on the URL mapped by the respective row (AKA Markov chain
	 * transition matrix). 
	 * @return stochastic matrix of probabilities 
	 */
	protected Matrix createTransitionMatrix() {
		int size = websites.size();
		double[][] matrix = new double[size][size];
		Collection<String> adjacent;
		for (int i=0; i<size; i++) {
			adjacent = connections.get(websites.get(i));
			int c = adjacent.size();
			double probability = 1d/c;
			for (int j=0; j<size; j++)
				 matrix[j][i] = adjacent.contains(websites.get(j)) ? probability : 0d;
		}
		return new Matrix(matrix);
	}
	
	/** The matrix that models leaving a page at any time */
	protected Matrix createTeleportationMatrix() {
		return Matrix.ones(websites.size(), 1);
	}
	
	public Map<String, Double> optimize(int iterations) {
		websites = new ArrayList<>(connections.keySet());
		int n = websites.size();
		if (n == 0)
			return new HashMap<>();
		// Cache website indices 
		cache = new HashMap<>();
		for (int i=0; i<n; i++)
			cache.put(websites.get(i), i);
		// Initialise constructs
		Matrix R = createInitialProbabilityVector(),	// PR(0)
				M = createTransitionMatrix(),
				T = createTeleportationMatrix();
		// Calculate page ranks vector : PR(i+1) = d . M . PR(i) + (1-d)/n . T 
		for (int i=0; i<iterations; i++) 
			R = M.times(d).times(R).plus(T.times((1d-d)/n));
		// Compile page ranks
		Map<String, Double> ranks = new HashMap<>();
		R.forEachVisitIndexed((i,j,val) -> ranks.put(websites.get(i), val));
		return lastResult = Maps.sortByValue(ranks);
	}

	public static void main(String[] args) {
		PageRank pr = new PageRank();
		// Example from : https://www.youtube.com/watch?v=kSmQbVxqOJc
		// and : https://www.youtube.com/watch?v=P8Kt6Abq_rM
		int iterations = 100;
		String A = "A", B = "B", C = "C", D = "D";
		pr.connections().put(A, Arrays.asList(C, B));
		pr.connections().put(B, Arrays.asList(D));
		pr.connections().put(C, Arrays.asList(A, B, D));
		pr.connections().put(D, Arrays.asList(C));
		Map<String, Double> ranks = pr.optimize(iterations);
		System.out.println(ranks);
		double sum = 0d;
		for (double val : ranks.values())
			sum += val;
		System.out.println(sum);
	}
}