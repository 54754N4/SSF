package test;

import graph.ComputeNode;

public class TestFramework {
	public static void main(String[] args) {
		testCompute();
	}
	
	public static void testCompute() {
		ComputeNode<String, Integer> node = Integer::parseInt;
		System.out.println(node.execute("4.0"));
	}
}
