package browser.common;

import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import io.github.bonigarcia.wdm.config.DriverManagerType;

public interface Configurators {

	static FirefoxConfigurator FIREFOX = new FirefoxConfigurator(); 
	static ChromeConfigurator CHROME = new ChromeConfigurator();
	static EdgeConfigurator EDGE = new EdgeConfigurator();
	static SafariConfigurator SAFARI = new SafariConfigurator(); 
	static OperaConfigurator OPERA = new OperaConfigurator(); 
	static InternetExplorerConfigurator INTERNET_EXPLORER = new InternetExplorerConfigurator(); 
	
	static FirefoxConfigurator firefox() {
		return FIREFOX;
	}
	
	static ChromeConfigurator chrome() {
		return CHROME;
	}
	
	static EdgeConfigurator edge() {
		return EDGE;
	}
	
	static SafariConfigurator safari() {
		return SAFARI;
	}
	
	static OperaConfigurator opera() {
		return OPERA;
	}
	
	static InternetExplorerConfigurator internetExplorer() {
		return INTERNET_EXPLORER;
	}
	
	/* Configurators */
	
	public static class FirefoxConfigurator extends BrowserConfigurator<FirefoxOptions> {
		public FirefoxConfigurator() {
			super(
					FirefoxOptions::new, 
					FirefoxDriver::new, 
					DriverManagerType.FIREFOX
			);
		}
	}
	
	public static class ChromeConfigurator extends BrowserConfigurator<ChromeOptions> {
		public ChromeConfigurator() {
			super(
					ChromeOptions::new, 
					ChromeDriver::new, 
					DriverManagerType.CHROME
			);
		}
	}
	
	public static class EdgeConfigurator extends BrowserConfigurator<EdgeOptions> {
		public EdgeConfigurator() {
			super(
					EdgeOptions::new, 
					EdgeDriver::new, 
					DriverManagerType.EDGE
			);
		}
	}
	
	public static class SafariConfigurator extends BrowserConfigurator<SafariOptions> {
		public SafariConfigurator() {
			super(
					SafariOptions::new, 
					SafariDriver::new, 
					DriverManagerType.SAFARI
			);
		}
	}
	
	public static class OperaConfigurator extends BrowserConfigurator<OperaOptions> {
		public OperaConfigurator() {
			super(
					OperaOptions::new, 
					OperaDriver::new, 
					DriverManagerType.OPERA
			);
		}
	}
	
	public static class InternetExplorerConfigurator extends BrowserConfigurator<InternetExplorerOptions> {
		public InternetExplorerConfigurator() {
			super(
					InternetExplorerOptions::new, 
					InternetExplorerDriver::new, 
					DriverManagerType.IEXPLORER
			);
		}
	}	
}