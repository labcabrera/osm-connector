package org.lab.osm.connector.validator;

import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class SerializationPrefixValidator implements Function<String, Boolean> {

	private static final String EXP = "^[\\w][\\w\\-]*$";

	@Override
	public Boolean apply(String prefix) {
		return StringUtils.isBlank(prefix) ? true : Pattern.compile(EXP).matcher(prefix).matches();
	}

}
