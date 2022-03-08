package browser.common;

import java.io.Closeable;
import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.print.PrintOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.openqa.selenium.support.locators.RelativeLocator.RelativeBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/* Creates a browser using Selenium that follows the builder pattern
 * to ease with chained calls/actions.
 * Docs: https://www.selenium.dev/documentation/en/webdriver/
 */
public class Browser implements Closeable {
    public static final long DEFAULT_TIMEOUT = 15, DEFAULT_POLLING = 5;		// in seconds
    private RemoteWebDriver driver;

    public Browser(RemoteWebDriver driver) {
        this.driver = driver;
    }
    
    public <K extends AbstractDriverOptions<?>> Browser(BrowserConfigurator<K> configurator) {
    	this(configurator.createDriver());
    }
    
    @Override
    public void close() {
        kill();
    }
    
    public void kill() {
        driver.quit();
    }
    
    private Browser handle(WebElement element, Collection<Consumer<WebElement>> consumers) {
        for (Consumer<WebElement> consumer : consumers)
            consumer.accept(element);
        return this;
    }
    
    /* Accessors and setters */
    
    public RemoteWebDriver getDriver() {
    	return driver;
    }

    public String getCurrentTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public Dimension getSize() {
        return driver.manage().window().getSize();
    }
    
    public Point getPosition() {	// of top-left corner
    	return driver.manage().window().getPosition();
    }
    
    public Browser setPosition(int x, int y) {
    	driver.manage().window().setPosition(new Point(x, y));
    	return this;
    }

    public Browser setSize(int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
        return this;
    }
    
    /* Browser methods */

    public Browser back() {
        driver.navigate().back();
        return this;
    }

    public Browser forward() {
        driver.navigate().forward();
        return this;
    }

    public Browser refresh() {
        driver.navigate().refresh();
        return this;
    }
    
    public Browser visit(String format, Object...args) {
        driver.navigate().to(String.format(format, args));
        return this;
    }
    
    public Browser visit(URL url) {
    	driver.navigate().to(url);
    	return this;
    }
    
	public Pdf print(Consumer<PrintOptions> consumer) {
    	PrintOptions options = new PrintOptions();
    	consumer.accept(options);
    	return driver.print(options);
    }
	
    /* DOM element methods */
    
    public WebElement findElement(By by) {
        return driver.findElement(by);
    }

    public WebElement findElement(RelativeBy by) {
    	return driver.findElement(by);
    }
    
    public WebElement findElement(By by, Function<RelativeBy, RelativeBy> relation) {
    	return driver.findElement(relation.apply(RelativeLocator.with(by)));
    }
        
    public List<WebElement> findElements(By by) {
        return driver.findElements(by);
    }

    public WebElement activeElement() {
    	return driver.switchTo().activeElement();
    }
    
    /* Window and tab and iframe methods */

    public Browser maximize() {
        driver.manage().window().maximize();
        return this;
    }
    
    public Browser minimize() {
    	driver.manage().window().minimize();
    	return this;
    }
    
    public Browser fullscreen() {
    	driver.manage().window().fullscreen();
    	return this;
    }
    
    public Browser newWindow(WindowType type) {
    	driver.switchTo().newWindow(type);
    	return this;
    }
    
    public Browser switchToNewTab() {
        return switchToNewTab(null);
    }

    public Browser switchToNewTab(@Nullable String href) {
        driver.switchTo().newWindow(WindowType.TAB);
        if (href != null) visit(href);
    	return this;
    }
    
    public Browser closeCurrentTab() {
        driver.close();
        return this;
    }
    
    public Browser switchToFrameContext(WebElement iframe) {
    	driver.switchTo().frame(iframe);
    	return this;
    }
    
    public Browser switchToParentContext() {
    	driver.switchTo().parentFrame();	// if top-level already == no side-effects
    	return this;
    }

    public Browser switchToDefaultContex() {
    	driver.switchTo().defaultContent();
    	return this;
    }
    
    /* JS execution */
    
    public JavascriptExecutor console() {
        return (JavascriptExecutor) driver;
    }

    public Object execute(String code, Object...args) {
    	return console().executeScript(code, args);
    }
    
    public Browser alert(String msg) {
    	execute(String.format("alert(\"%s\")", msg));
    	return this;
    }
    
    public Alert waitForAlert() {
    	return waitFor().until(ExpectedConditions.alertIsPresent());
    }
    
    public Object prompt(String msg) {
    	return prompt(msg, null);
    }
    
    public Object prompt(String msg, @Nullable String defaultValue) {
    	Object response;
    	if (defaultValue != null) 
    		response = execute(String.format("prompt(\"%s\", \"%s\")", msg, defaultValue));
    	else 
    		response = execute(String.format("prompt(\"%s\")", msg));
    	return response;
    }
    
    /* Cookie handling */

    public Browser deleteAllCookies() {
        driver.manage().deleteAllCookies();
        return this;
    }

    public Browser deleteCookie(String name) {
        driver.manage().deleteCookieNamed(name);
        return this;
    }

    public Browser deleteCookie(Cookie cookie) {
        driver.manage().deleteCookie(cookie);
        return this;
    }

    public Browser addCookie(Cookie cookie) {
        driver.manage().addCookie(cookie);
        return this;
    }

    public Set<Cookie> getCookies() {
        return driver.manage().getCookies();
    }

    public Cookie getCookie(String name) {
        return driver.manage().getCookieNamed(name);
    }

    public Browser forEachCookie(Consumer<Cookie> action) {
        return forEachCookie(always -> true, action);
    }

    public Browser forEachCookie(Predicate<Cookie> filter, Consumer<Cookie> action) {
        return forEach(this::getCookies, filter, action);
    }

    public Browser forEachCookieIndexed(BiConsumer<Integer, Cookie> action) {
        return forEachIndexed(this::getCookies, always -> true, action);
    }

    public Browser forEachCookieIndexed(Predicate<Cookie> filter, BiConsumer<Integer, Cookie> action) {
        return forEachIndexed(this::getCookies, filter, action);
    }

    public Browser printCookies() {
        return forEachCookieIndexed(Browser::defaultPrinter);
    }

    /* Screenshot handling */

    private <T> T screenshot(TakesScreenshot screenshot, OutputType<T> type) {
        return screenshot.getScreenshotAs(type);
    }

    public <T> T screenshotFullAs(OutputType<T> type) {
        return screenshotOf(By.tagName("body"), type);
    }

    public File screenshotFullAsFile() {
        return screenshotFullAs(OutputType.FILE);
    }

    public File screenshotAsFile() {
        return screenshotAs(OutputType.FILE);
    }

    public File screenshotFileOf(By by) {
        return screenshotOf(by, OutputType.FILE);
    }

    public <T> T screenshotAs(OutputType<T> type) {
        return screenshot(driver, type);
    }

    public <T> T screenshotOf(By by, OutputType<T> type) {
        return screenshot(driver.findElement(by), type);
    }

    /* Wait/delays handling */

    /* This will slow all browser requests. Use with care or only
     * if you know what you're doing. 
     **/
    public Browser waitImplicitly(long timeout) {
    	driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeout));
    	return this;
    }
    
    public WebDriverWait waitFor() {
    	return waitFor(DEFAULT_TIMEOUT);
    }
    
    public WebDriverWait waitFor(long seconds) {
    	return waitFor(Duration.ofSeconds(seconds));
    }
    
    public WebDriverWait waitFor(Duration duration) {
    	return new WebDriverWait(driver, duration);
    }
    
    public WebElement waitGet(By by) {
        return waitUntil(driver -> driver.findElement(by));
    }

    public List<WebElement> waitGetAll(By by) {
        return waitUntil(driver -> driver.findElements(by));
    }
    
    public <T> T waitUntil(ExpectedCondition<T> condition) {
    	return waitFor().until(condition);
    }
    
    public <V> V waitUntil(Function<? super WebDriver, V> isTrue) {
        try {
            return new FluentWait<>(driver)
                    .until(isTrue);
        } catch (Exception e) {
            return null;
        }
    }

    public Browser waitFor(Function<? super WebDriver, Boolean> isTrue) {
        waitFor().until(isTrue);
        return this;
    }

    public Browser waitFor(By by, Collection<Consumer<WebElement>> consumers) {
        WebElement element = waitFor().until(driver -> driver.findElement(by));
        return handle(element, consumers);
    }

    public Browser waitForFluent(By by, long timeout, long polling, Collection<Class<? extends Throwable>> exceptions, Collection<Consumer<WebElement>> consumers) {
        WebElement element = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(polling))
                .ignoreAll(exceptions)
                .until(driver -> driver.findElement(by));
        return handle(element, consumers);
    }

    public Browser waitForFluent(By by, long timeout, long polling, Class<? extends Throwable> exception, Collection<Consumer<WebElement>> actions) {
        return waitForFluent(by, timeout, polling, Arrays.asList(exception), actions);
    }

    public Browser waitForFluent(By by, long timeout, long polling, Collection<Consumer<WebElement>> actions) {
        return waitForFluent(by, timeout, polling, NoSuchElementException.class, actions);
    }

    public Browser waitForFluent(By by, long timeout, long polling, Class<? extends Throwable> exception) {
        return waitForFluent(by, timeout, polling, Arrays.asList(exception), Arrays.asList());
    }

    public Browser waitForFluent(By by, long timeout, long polling) {
        return waitForFluent(by, timeout, polling, NoSuchElementException.class, Arrays.asList());
    }

    public Browser waitUntilLoaded() {
    	waitFor().until(pageLoadedCondition());
        return this;
    }

    public final ExpectedCondition<Boolean> pageLoadedCondition() {
        return d -> console()
                .executeScript("return document.readyState;")
                .equals("complete");
    }

    /* User handling + simulated actions */
    
    public Actions actions() {
        return new Actions(driver);
    }

    public Browser type(final String input, By by) {
        return waitFor(by, Arrays.asList(element -> element.sendKeys(input)));
    }

    public Browser click(By...bys) {
        return click(null, bys);
    }

    public Browser click(@Nullable Consumer<Throwable> onError, By...bys) {
        for (By by : bys)
            try {
                waitGet(by).click();
            } catch (Exception e) {
                if (onError != null)
                    onError.accept(e);
            }
        return this;
    }

    public Browser tryClick(By...selectors) {
        for (By selector : selectors)
            try { driver.findElement(selector).click(); }
            catch (Exception e) { /* swallow  exception */ }
        return this;
    }

    public Browser scrollTo(By target) {
        WebElement element = waitFor().until(ExpectedConditions.presenceOfElementLocated(target));
        return scrollTo(element);
    }

    public Browser scrollTo(WebElement element) {
        actions().moveToElement(element).perform();
        return this;
    }

    public Browser scrollTo(int x, int y) {
        Point window = driver.manage().window().getPosition();
        return scrollBy(x - window.x, y - window.y);
    }

    public Browser scrollBy(int x, int y) {
        actions().moveByOffset(x, y).perform();
        return this;
    }

    public Browser scrollToJS(By target) {
        WebElement element = waitFor().until(ExpectedConditions.presenceOfElementLocated(target));
        return scrollToJS(element);
    }

    public Browser scrollToJS(WebElement element) {
        Point location = element.getLocation();
        return scrollToJS(location.x, location.y);
    }

    public Browser scrollToJS(int x, int y) {
        execute("window.scrollTo(arguments[0], arguments[1]);", x, y);
        return this;
    }

    public Browser scrollByJS(int x, int y) {
        execute("window.scrollBy(arguments[0], arguments[1]);", x, y);
        return this;
    }

    public Browser scrollAxisBy(boolean abscissa, int delta) {
        String offset = ""+delta,
                code = String.format("window.scrollBy(%s,%s);",
                        abscissa ? offset : "window.scrollX",
                        abscissa ? "window.scrollY" : offset);
        execute(code);
        return this;
    }

    public Browser scrollHorizontallyBy(int dx) {
        return scrollAxisBy(true, dx);
    }

    public Browser scrollVerticallyBy(int dy) {
        return scrollAxisBy(false, dy);
    }

    public Browser scrollIntoView(By target) {
        WebElement element = waitFor().until(ExpectedConditions.presenceOfElementLocated(target));
        return scrollIntoView(element);
    }

    public Browser scrollIntoView(
            By target,
            ScrollOptions.Behavior behavior,
            ScrollOptions.Block block,
            ScrollOptions.Inline inline) {
        WebElement element = waitFor().until(ExpectedConditions.presenceOfElementLocated(target));
        return scrollIntoView(element, behavior, block, inline);
    }

    public Browser scrollIntoView(WebElement element) {
        execute("arguments[0].scrollIntoView();", element);
        return this;
    }

    public Browser scrollIntoView(
            WebElement element,
            ScrollOptions.Behavior behavior,
            ScrollOptions.Block block,
            ScrollOptions.Inline inline) {
        String code = String.format("arguments[0].scrollIntoView({behavior: '%s', block: '%s', inline: '%s'});",
                ScrollOptions.getValue(behavior),
                ScrollOptions.getValue(block),
                ScrollOptions.getValue(inline));
        execute(code, element);
        return this;
    }

    public Browser hover(WebElement element) {
        actions().moveToElement(element).perform();
        return this;
    }

    public Browser hover(By target) {
        return hover(waitFor().until(ExpectedConditions.presenceOfElementLocated(target)));
    }

    public Browser hoverJS(By target) {
        String code = "var evObj = document.createEvent('MouseEvents');" +
                "evObj.initMouseEvent(\"mouseover\",true, false, window, 0, 0, 0, 0, 0, false, false, false, false, 0, null);" +
                "arguments[0].dispatchEvent(evObj);";
        WebElement element = waitFor().until(ExpectedConditions.presenceOfElementLocated(target));
        execute(code, element);
        return this;
    }

    /* Convenience methods */

    public <T> Browser forEach(Supplier<Collection<T>> supplier, Consumer<T> action) {
        return forEach(supplier, e -> true, action);
    }

    public <T> Browser forEach(Supplier<Collection<T>> supplier, Predicate<T> filter, Consumer<T> action) {
        for (T element : supplier.get())
            if (filter.test(element))
                action.accept(element);
        return this;
    }

    public <T> Browser forEachIndexed(Supplier<Collection<T>> supplier, Predicate<T> filter, BiConsumer<Integer, T> action) {
        int index = 0;
        for (T element : supplier.get())
            if (filter.test(element))
                action.accept(index++, element);
        return this;
    }

    public static <K> void defaultPrinter(int index, K c) {
    	System.out.printf("%d. %s%n", index, c.toString());
    }

    public static class ScrollOptions {
        public static enum Behavior { SMOOTH, AUTO }
        public static enum Block { START, CENTER, END, NEAREST }
        public static enum Inline { START, CENTER, END, NEAREST }

        public static <E extends Enum<E>> String getValue(Enum<E> e) {
            return e.name().toLowerCase();
        }
    }
}
