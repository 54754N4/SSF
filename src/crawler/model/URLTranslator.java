package crawler.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * URL pattern matching/filtering translator. Converts
 * from simple pattern matching queries to correct regex
 * wildcards.
 */
public abstract class URLTranslator {
	public static final Map<String, String> PATTERN_2_REGEX_RULES;
	
	public static final String 
	DOT = "\\.", DOT_ESCAPED = "\\\\.",			// . -> \.
	ANY = "\\*", ANY_REGEX = ".*",				// * -> .*
	ANY_CHAR = "\\?", ANY_CHAR_REGEX = ".";		// ? -> .
	
	static {
		// Inserting order is important because values get replaced
		// from top to bottom and there might be clashes (for example
		// if DOT is replaced after ANY then the mapped ANY_REGEX value
		// would be modified as well).
		PATTERN_2_REGEX_RULES = new ConcurrentHashMap<>();
		PATTERN_2_REGEX_RULES.put(DOT, DOT_ESCAPED);
		PATTERN_2_REGEX_RULES.put(ANY, ANY_REGEX);
		PATTERN_2_REGEX_RULES.put(ANY_CHAR, ANY_CHAR_REGEX);
	}
	
	public static String translate(String pattern) {
		for (Map.Entry<String, String> entry : PATTERN_2_REGEX_RULES.entrySet())
			pattern = pattern.replaceAll(entry.getKey(), entry.getValue());
		return pattern;
	}
}