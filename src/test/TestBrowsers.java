package test;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import browser.common.Browser;
import browser.common.BrowserConfigurator.Builder;
import browser.common.Options;
import browser.common.Configurators;

public class TestBrowsers {
	public static void main(String[] args) throws InterruptedException {
		testFirefox();
//		testChrome();
	}
	
	public static void testFirefox() throws InterruptedException {
		Builder<FirefoxOptions> builder = Configurators.firefox()
				.config(Options.FIREFOX::defaultSettings)
				.config(Options.FIREFOX::debugging);
		try (Browser browser = new Browser(builder.build())) {
			System.out.println(browser.getCurrentUrl());
			browser.visit("http://www.google.com")
				.waitUntilLoaded();
			System.out.println(browser.getCurrentUrl());
			Thread.sleep(5000);
		}
	}
	
	public static void testChrome() throws InterruptedException {
		Builder<ChromeOptions> builder = Configurators.chrome()
				.config(Options.CHROME::defaultSettings)
				.config(Options.CHROME::debugging);
		try (Browser browser = new Browser(builder.build())) {
			System.out.println(browser.getCurrentUrl());
			browser.visit("http://www.google.com")
				.waitUntilLoaded();
			System.out.println(browser.getCurrentUrl());
			Thread.sleep(5000);
		}
	}
}
