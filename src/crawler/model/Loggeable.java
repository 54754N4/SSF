package crawler.model;

import browser.common.Constants;

/* Logging methods */
public interface Loggeable {
	
	default void log(String format, Object...args) {
		if (Constants.VERBOSE) 
			System.out.printf(this+"\t| "+format, args);
	}
	
	default void logln(String format, Object...args) {
		log(format+"%n", args);
	}
}
