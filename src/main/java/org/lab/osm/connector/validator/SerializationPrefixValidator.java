package org.lab.osm.connector.validator;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Serialization file prefix validator.
 * 
 * @author lab.cabrera@gmail.com
 * @since 1.1.0
 */
public class SerializationPrefixValidator implements Function<String, Boolean> {

	private static final String EXP = "^[\\w][\\w\\-]*$";

	/* (non-Javadoc)
	 * @see java.util.function.Function#apply(java.lang.Object)
	 */
	@Override
	public Boolean apply(String prefix) {
		return StringUtils.isBlank(prefix) ? true : Pattern.compile(EXP).matcher(prefix).matches();
	}

}
