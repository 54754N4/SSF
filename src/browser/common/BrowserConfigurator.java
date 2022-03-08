package browser.common;

import java.util.function.Function;
import java.util.function.Supplier;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.bonigarcia.wdm.config.DriverManagerType;

public class BrowserConfigurator<K> {
	private final Supplier<K> options;
	private final Function<K, RemoteWebDriver> creator;
	private final DriverManagerType type;
	
	private BrowserConfigurator(Supplier<K> options, Function<K, RemoteWebDriver> creator, DriverManagerType type) {
		this.options = options;
		this.creator = creator;
		this.type = type;
	}
	
	/* Checks that a specific browser driver is already 
	 * downloaded, otherwise downloads correct version.
	 */
	public RemoteWebDriver createDriver() {
		WebDriverManager manager = WebDriverManager.getInstance(type);
		manager = Constants.is64Bit ? manager.arch64() : manager.arch32();
		manager.setup();
		return creator.apply(options.get());
	}
	
	/**
	 * Centralises building + convenience methods
	 * for options (allows generic methods like
	 * Builder::setProxy).
	 * 
	 * @param <K> - browser options runtime class type
	 */
	public static class Builder<K extends AbstractDriverOptions<?>> {
		protected Supplier<K> options;
		protected Function<K, RemoteWebDriver> creator;
		protected DriverManagerType type;
		
		/* Package private constructor */
		Builder(Supplier<K> options, Function<K, RemoteWebDriver> creator, DriverManagerType type) {
			this.options = options;
			this.creator = creator;
			this.type = type;
		}
		
		public Builder<K> config(K options) {
			this.options = () -> options;
			return this;
		}
		
		public Builder<K> config(Supplier<K> options) {
			this.options = options;
			return this;
		}

		public <V> Builder<K> config(Function<K, V> configurator) {
			final K ops = options.get();
			configurator.apply(ops);
			this.options = () -> ops;
			return this;
		}
		
		/* Convenience methods */
		
		public Builder<K> setProxy(final String host, final int port) {
			return config(ops -> ops.setProxy(new Proxy().setHttpProxy(String.format("%s:%s", host, port))));
		}
		
		public Builder<K> acceptInsecureCerts(boolean bool) {
			return config(ops -> ops.setAcceptInsecureCerts(bool));
		}
		
		public BrowserConfigurator<K> build() {
			return new BrowserConfigurator<>(options, creator, type);
		}
	}
}