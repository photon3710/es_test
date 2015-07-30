package com.qd.util;

import java.util.Properties;

/**
 * This class is used to find value for the prefixed key. It is useful
 * in the situations where multiple instances of the same class needed to
 * be initialized.
 * 
 * @author xiaoyun
 *
 */
public class OptionAccessor {
	private final String prefix;
	private final Properties properties;
	
	public OptionAccessor(Properties p, String pfx) {
		if (pfx.endsWith(".")) throw new RuntimeException("prefix should not end with dot");
		this.prefix = pfx == "" ? pfx : pfx + ".";
		this.properties = p;
	}
	
	public String getFullKey(String key) {
		return prefix + key;
	}
	
	public boolean hasValue(String key) {
		return properties.containsKey(getFullKey(key));
	}
	
	public String getValue(String key) {
		return properties.getProperty(getFullKey(key));
	}
	
	public String getValue(String key, String defValue) {
		return properties.getProperty(getFullKey(key), defValue);
	}
	
	/**
	 * This method allow one to project underlying properties along the given prefix
	 * to form another properties. Here projection simply means find the subset of key/value
	 * pair where key has the given prefix, but the prefix is striped away for resulting prefix.
	 * 
	 * @param keys
	 * @return projected properties.
	 */
	public Properties getProjection() {
		Properties res = new Properties();
		for (String key: properties.stringPropertyNames()) {
			if (prefix.length() == 0 || key.startsWith(prefix)) {
				res.setProperty(key.substring(prefix.length()), getValue(key));
			}
		}
		return res;
	}
}
