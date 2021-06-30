package browser.common;

import java.util.function.Function;
import java.util.function.Supplier;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

public abstract class BrowserConfigurator<K> {
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
	
	private final Supplier<K> options;
	private final Function<K, RemoteWebDriver> creator;
	private final DriverManagerType type;
	
	public BrowserConfigurator(Supplier<K> options, Function<K, RemoteWebDriver> creator, DriverManagerType type) {
		this.options = options;
		this.creator = creator;
		this.type = type;
	}
	
	public K createOptions() {
		return options.get();
	}
	
	// checks that driver is downloaded, otherwise downloads it
	private static void setupDriver(DriverManagerType type) {
		WebDriverManager manager = WebDriverManager.getInstance(type);
		manager = is64Bit ? manager.arch64() : manager.arch32();
		manager.setup();
	}
	
	/* Browser creation */
	
	public RemoteWebDriver createDriver(K options) {
		setupDriver(type);
		return creator.apply(options);
	}
	
	public <R extends K> RemoteWebDriver createDriver(Function<K, R> configuration) {
		setupDriver(type);
		return creator.apply(configuration.apply(options.get()));
	}
	
	public RemoteWebDriver createDriver(Supplier<K> configuration) {
		setupDriver(type);
		return creator.apply(configuration.get());
	}
	
	public RemoteWebDriver createDriver(Pipeline<Void, K> configuration) {
		setupDriver(type);
		return creator.apply(configuration.run());
	}
	
	/* Convenience methods */
	
	public Proxy createProxy(String host, int port) {
		return new Proxy()
				.setHttpProxy(String.format("%s:%d", host, port));
	}
}