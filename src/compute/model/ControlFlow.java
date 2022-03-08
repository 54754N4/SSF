package compute.model;

public abstract class ControlFlow extends DataFlow<Void, Void> {
	
	public abstract void execute() throws Exception;
	
	@Override
	public Void execute(Void in) throws Exception {
		execute();
		return null;
	}
	
	public void run() throws Exception {
		run(null);
	}
	
	public static ControlFlow from(Runnable runnable) {
		return new ControlFlow() {
			@Override
			public void execute() throws Exception {
				runnable.run();
			}
		};
	}
}
 