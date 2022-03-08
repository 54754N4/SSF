package browser.common;

public abstract class Constants {
	public static final boolean is64Bit, is32Bit;
	
	/* Check host OS bit-ness on initial reference to class
	 * Reference: https://stackoverflow.com/a/5940770/3225638
	 */
	static {
		String arch = System.getenv("PROCESSOR_ARCHITECTURE");
		String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
		is64Bit = arch != null && arch.endsWith("64")
                || wow64Arch != null && wow64Arch.endsWith("64");
		is32Bit = !is64Bit;
	}
	
	public static final boolean VERBOSE = true;	// for debugging
}
