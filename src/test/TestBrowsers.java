package test;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import browser.common.Browser;
import browser.common.Configurations;
import browser.common.Configurators;
import browser.common.Pipeline;

public class TestBrowsers {
	public static void main(String[] args) throws InterruptedException {
//		testFirefox();
		testChrome();
	}
	
	public static void testFirefox() throws InterruptedException {
		Pipeline<Void, FirefoxOptions> pipeline = Pipeline.start(Configurations.FIREFOX::defaultSettings)
			.then(Configurations.FIREFOX::debugging);
		try (Browser browser = new Browser(Configurators.firefox().setOptions(pipeline).build())) {
			System.out.println(browser.getCurrentUrl());
			browser.visit("http://www.google.com")
				.waitUntilLoaded();
			System.out.println(browser.getCurrentUrl());
			Thread.sleep(5000);
		}
	}
	
	public static void testChrome() throws InterruptedException {
		Pipeline<Void, ChromeOptions> pipeline = Pipeline.start(Configurations.CHROME::defaultSettings)
				.then(Configurations.CHROME::debugging);
		try (Browser browser = new Browser(Configurators.chrome().setOptions(pipeline).build())) {
			System.out.println(browser.getCurrentUrl());
			browser.visit("http://www.google.com")
				.waitUntilLoaded();
			System.out.println(browser.getCurrentUrl());
			Thread.sleep(5000);
		}
	}
}
