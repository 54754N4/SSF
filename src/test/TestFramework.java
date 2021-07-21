package test;

import compute.ResilientAction;

public class TestFramework {
	public static void main(String[] args) {
		testCompute();
	}
	
	public static void testCompute() {
		ResilientAction<String, Integer> node = Integer::parseInt;
		System.out.println(node.execute("4.0"));
	}
}
