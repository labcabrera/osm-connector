package org.lab.osm.connector.validator;

import java.util.function.Function;
import java.util.regex.Pattern;

public class PackageNameValidator implements Function<String, Boolean> {

	private static final String EXP = "^[a-zA-Z][\\w\\d]*(\\.[a-zA-Z][\\w\\d]*)*$";

	@Override
	public Boolean apply(String packageName) {
		return Pattern.compile(EXP).matcher(packageName).matches();
	}

}
