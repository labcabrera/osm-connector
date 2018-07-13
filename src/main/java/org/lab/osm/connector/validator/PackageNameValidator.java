package org.lab.osm.connector.validator;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Package validator.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.1.0
 */
public class PackageNameValidator implements Function<String, Boolean> {

	private static final String EXP = "^[a-zA-Z][\\w\\d]*(\\.[a-zA-Z][\\w\\d]*)*$";

	/* (non-Javadoc)
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	@Override
	public Boolean apply(String packageName) {
		return Pattern.compile(EXP).matcher(packageName).matches();
	}

}
