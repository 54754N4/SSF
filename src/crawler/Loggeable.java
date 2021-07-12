package crawler;

import browser.common.Constants;

/* Logging methods */
public interface Loggeable {
	
	default void log(String format, Object...args) {
		if (Constants.DEBUG) 
			System.out.printf(this+"|"+format, args);
	}
	
	default void logln(String format, Object...args) {
		log(format+"%n", args);
	}
}
