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
	
	static BrowserConfigurator.Builder<FirefoxOptions> firefox() {
		return new BrowserConfigurator.Builder<>(
			FirefoxOptions::new,
			FirefoxDriver::new,
			DriverManagerType.FIREFOX);
	}
	
	static BrowserConfigurator.Builder<ChromeOptions> chrome() {
		return new BrowserConfigurator.Builder<>(
			ChromeOptions::new,
			ChromeDriver::new,
			DriverManagerType.CHROME);
	}
	
	static BrowserConfigurator.Builder<EdgeOptions> edge() {
		return new BrowserConfigurator.Builder<>(
			EdgeOptions::new,
			EdgeDriver::new,
			DriverManagerType.EDGE);
	}
	
	static BrowserConfigurator.Builder<SafariOptions> safari() {
		return new BrowserConfigurator.Builder<>(
			SafariOptions::new,
			SafariDriver::new,
			DriverManagerType.SAFARI);
	}
	
	static BrowserConfigurator.Builder<OperaOptions> opera() {
		return new BrowserConfigurator.Builder<>(
			OperaOptions::new,
			OperaDriver::new,
			DriverManagerType.OPERA); 
	}
	
	static BrowserConfigurator.Builder<InternetExplorerOptions> internetExplorer() {
		return new BrowserConfigurator.Builder<>(
			InternetExplorerOptions::new,
			InternetExplorerDriver::new,
			DriverManagerType.IEXPLORER);
	}
}