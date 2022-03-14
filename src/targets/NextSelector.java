package targets;

import java.io.Closeable;
import java.util.function.Function;
import java.util.function.Predicate;

import browser.common.Browser;
import browser.common.Configurators;
import browser.common.Options;

public class NextSelector implements TargetSelector, Closeable {
	private Browser browser;
	private Predicate<Browser> checker;
	private Function<Browser, String> nextSelector;
	
	public NextSelector(String initialUrl, Predicate<Browser> checker, Function<Browser, String> nextSelector, Browser browser) {
		this.browser = browser;
		this.checker = checker;
		this.nextSelector = nextSelector;
		browser.visit(initialUrl);
	}
	
	@Override
	public boolean hasNext() {
		return checker.test(browser);
	}

	@Override
	public String next() {
		return nextSelector.apply(browser);
	}
	
	@Override
	public void close() {
		browser.close();
	}

	public static class Builder {
		private String initialUrl;
		private Browser browser;
		private Predicate<Browser> checker;
		private Function<Browser, String> nextSelector;
		
		public Builder setInitialUrl(String initialUrl) {
			this.initialUrl = initialUrl;
			return this;
		}
		
		public Builder setBrowser(Browser browser) {
			this.browser = browser;
			return this;
		}
		
		public Builder setChecker(Predicate<Browser> checker) {
			this.checker = checker;
			return this;
		}
		
		public Builder setNextSelector(Function<Browser, String> nextSelector) {
			this.nextSelector = nextSelector;
			return this;
		}
		
		public NextSelector build() {
			if (initialUrl == null)
				throw new IllegalArgumentException("Initial URL is null");
			if (checker == null)
				throw new IllegalArgumentException("Page element checker is null");
			if (nextSelector == null)
				throw new IllegalArgumentException("Next selector is null");
			if (browser == null) // By default, use firefox
				browser = new Browser(Configurators.firefox()
					.config(Options.FIREFOX::defaultSettings)
//					.config(Options.FIREFOX::debugging)
					.build());
			return new NextSelector(initialUrl, checker, nextSelector, browser);
		}
	}
}
