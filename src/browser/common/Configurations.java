package browser.common;

import java.awt.Dimension;
import java.awt.Toolkit;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.safari.SafariOptions;

public interface Configurations {
	
	/* Constants */
	
	static final Dimension SCREEN = Toolkit.getDefaultToolkit().getScreenSize();

	/* Driver arguments */
	
	static final String
		ARG_DISABLE_GPU = "--disable-gpu",
		ARG_DISABLE_EXTENSIONS = "--disable-extensions",
		ARG_WINDOW_SIZE = String.format("--window-size=%s,%s", SCREEN.width, SCREEN.height);
	
	/* Specific browser configs */
	
	static Firefox FIREFOX = new Firefox();
	static Chrome CHROME = new Chrome();
	static Edge EDGE = new Edge();
	static Safari SAFARI = new Safari();
	static Opera OPERA = new Opera();
	static InternetExplorer INTERNET_EXPLORER = new InternetExplorer();
	
	static Firefox firefox() {
		return FIREFOX;
	}
	
	static Chrome chrome() {
		return CHROME;
	}
	
	static Edge edge() {
		return EDGE;
	}
	
	static Safari safari() {
		return SAFARI;
	}
	
	static Opera opera() {
		return OPERA;
	}
	
	static InternetExplorer internetExplorer() {
		return INTERNET_EXPLORER;
	}
	
	/* All configuration methods that need to be created for each browser */
	
	static interface ConfigurationMethods<T> {
		T defaultSettings();
		default T debugging(T options) {
			throw new IllegalStateException("Browser doesn't support headless and other debugging features.");
		}
		
	}
	
	public static class Firefox implements ConfigurationMethods<FirefoxOptions> {
		@Override
		public FirefoxOptions defaultSettings() {
			return new FirefoxOptions()
					.setHeadless(true)
					.setAcceptInsecureCerts(true)
                    .addArguments(ARG_DISABLE_GPU, ARG_WINDOW_SIZE);
		}

		@Override
		public FirefoxOptions debugging(FirefoxOptions options) {
			return options.setHeadless(false);
		}
	}

	public static class Chrome implements ConfigurationMethods<ChromeOptions> {
		@Override
		public ChromeOptions defaultSettings() {
			return (ChromeOptions) new ChromeOptions()
					.setHeadless(true)
					.setAcceptInsecureCerts(true)
	                .addArguments(ARG_DISABLE_GPU, ARG_WINDOW_SIZE);
		}
	
		@Override
		public ChromeOptions debugging(ChromeOptions options) {
			return options.setHeadless(false);
		}
	}
	
	public static class Edge implements ConfigurationMethods<EdgeOptions> {	
		@Override
		public EdgeOptions defaultSettings() {
			return (EdgeOptions) new EdgeOptions()
					.setHeadless(true)
					.setAcceptInsecureCerts(true)
	                .addArguments(ARG_DISABLE_GPU, ARG_WINDOW_SIZE);
		}
	
		@Override
		public EdgeOptions debugging(EdgeOptions options) {
			return options.setHeadless(false);
		}
	}
	
	public static class Safari implements ConfigurationMethods<SafariOptions> {
		@Override
		public SafariOptions defaultSettings() {
			return new SafariOptions()
					.setAcceptInsecureCerts(true);
		}
	}
	
	public static class Opera implements ConfigurationMethods<OperaOptions> {
		@Override
		public OperaOptions defaultSettings() {
			return (OperaOptions) new OperaOptions()
					.setAcceptInsecureCerts(true)
	                .addArguments(ARG_DISABLE_GPU, ARG_WINDOW_SIZE);
		}
	}
	
	public static class InternetExplorer implements ConfigurationMethods<InternetExplorerOptions> {
		@Override
		public InternetExplorerOptions defaultSettings() {
			return new InternetExplorerOptions()
					.setAcceptInsecureCerts(true);
		}
	}
}
