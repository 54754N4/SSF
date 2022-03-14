package targets;

public class PresetSelector implements TargetSelector {
	private String[] targets;
	private int i;

	public PresetSelector(String...targets) {
		this.targets = targets;
		i = 0;
	}

	@Override
	public boolean hasNext() {
		return i < targets.length;
	}

	@Override
	public String next() {
		return targets[i++];
	}
}
