package test;

import compute.element.ResilientAction;
import compute.model.ControlFlow;
import compute.model.DataFlow;

public class TestFramework {
	public static void main(String[] args) throws Exception {
//		testResilientAction();
//		testControlFlow();
		testDataFlow();
	}
	
	public static void testResilientAction() {
		ResilientAction<String, Integer> node = Integer::parseInt;
		System.out.println(node.execute("4"));
		System.out.println(node.execute("4.0"));
		System.out.println(ResilientAction.<String, Integer>wrap(Integer::parseInt).execute("3"));
	}
	
	public static void testControlFlow() throws Exception {
		final long INITIAL = System.currentTimeMillis();
		class TestControlFlow extends ControlFlow {
			private int count;
			
			public TestControlFlow(int count) {
				this.count = count;
			}
			
			@Override
			public void execute() throws Exception {
				Thread.sleep(2000);
				System.out.println(count+" finished in "+(System.currentTimeMillis() - INITIAL));
			}
		}
		/*	    f4 - f6
		       /
		     f2 
		 	/  \
		 f1	    f5
		 	\ 
		 	 f3
		*/
		ControlFlow f1 = new TestControlFlow(1),
			f2 = new TestControlFlow(2),
			f3 = new TestControlFlow(3),
			f4 = new TestControlFlow(4),
			f5 = new TestControlFlow(5),
			f6 = new TestControlFlow(6);
		f1.connect(f2);
		f1.connect(f3);
		f2.connect(f4);
		f2.connect(f5);
		f4.connect(f6);
		f1.run();
	}
	
	public static void testDataFlow() throws Exception {
		class Data {
			private int data;
			
			public Data(int data) {
				this.data = data;
			}
			
			@Override
			public String toString() {
				return "Data is : "+data;
			}
		}
		/*     f4 - f5 - f6
		 	  /
		    f2
		   /
		 f1
		   \
		    f3
		 */
		DataFlow<String, String> f1 = DataFlow.from(String::trim);
		DataFlow<String, Integer> f2 = DataFlow.from(String::length);
		DataFlow<String, Void> f3 = DataFlow.from(str -> { System.out.println("Trimmed input is : "+str); return null; });
		DataFlow<Integer, Data> f4 = DataFlow.from(integer -> new Data(integer));
		DataFlow<Data, String> f5 = DataFlow.from(Data::toString);
		DataFlow<String, Void> f6 = DataFlow.from(str -> { System.out.println(str); return null; });
		f1.connect(f2);
		f1.connect(f3);
		f2.connect(f4);
		f4.connect(f5);
		f5.connect(f6);
		f1.run("     abc    ");
	}
}

