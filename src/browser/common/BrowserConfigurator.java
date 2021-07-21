package browser.common;

import java.util.function.Function;
import java.util.function.Supplier;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

public class BrowserConfigurator<K> {
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
	
	private BrowserConfigurator(Supplier<K> options, Function<K, RemoteWebDriver> creator, DriverManagerType type) {
		this.options = options;
		this.creator = creator;
		this.type = type;
	}
	
	public RemoteWebDriver createDriver() {
		// Checks that driver is downloaded, otherwise downloads it
		WebDriverManager manager = WebDriverManager.getInstance(type);
		manager = is64Bit ? manager.arch64() : manager.arch32();
		manager.setup();
		return creator.apply(options.get());
	}
	
	/**
	 * Centralises building + convenience methods
	 * for options (allows generic methods like
	 * Builder::setProxy).
	 * 
	 * @param <K> - browser options class type
	 */
	public static class Builder<K extends AbstractDriverOptions<?>> {
		protected Supplier<K> options;
		protected Function<K, RemoteWebDriver> creator;
		protected DriverManagerType type;
		
		/**
		 * Package private constructor
		 */
		Builder(Supplier<K> options, Function<K, RemoteWebDriver> creator, DriverManagerType type) {
			this.options = options;
			this.creator = creator;
			this.type = type;
		}
		
		public Builder<K> setOptions(K options) {
			this.options = () -> options;
			return this;
		}
		
		public Builder<K> setOptions(Supplier<K> options) {
			this.options = options;
			return this;
		}
		
		public Builder<K> setOptions(Pipeline<Void, K> configuration) {
			this.options = configuration::run;
			return this;
		}
		
		public Builder<K> config(Function<K, K> configurator) {
			this.options = () -> configurator.apply(options.get());
			return this;
		}

		/* Convenience methods */
		
		public Builder<K> setProxy(final String host, final int port) {
			this.options = () -> {
				K ops = options.get();
				Proxy proxy = new Proxy()
					.setHttpProxy(String.format("%s:%s", host, port));
				ops.setCapability("proxy", proxy);
				return ops;
			};
			return this;	
		}
		
		public BrowserConfigurator<K> build() {
			return new BrowserConfigurator<>(options, creator, type);
		}
	}
}