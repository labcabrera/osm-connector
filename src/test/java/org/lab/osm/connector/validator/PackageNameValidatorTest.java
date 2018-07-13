package org.lab.osm.connector.validator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class PackageNameValidatorTest {

	@Test
	public void testOk01() {
		Assert.assertTrue(new PackageNameValidator().apply("org.lab.osm"));
	}

	@Test
	public void testOk02() {
		Assert.assertTrue(new PackageNameValidator().apply("org.lab.osm"));
	}

	@Test
	public void testOk03() {
		Assert.assertTrue(new PackageNameValidator().apply("org.lab21.osm42"));
	}

	@Test
	public void testOk04() {
		Assert.assertTrue(new PackageNameValidator().apply("a.b.c"));
	}

	@Test
	public void testOk05() {
		Assert.assertTrue(new PackageNameValidator().apply("package"));
	}

	@Test
	public void testKo01() {
		Assert.assertFalse(new PackageNameValidator().apply(".org.lab.osm"));
	}

	@Test
	public void testKo02() {
		Assert.assertFalse(new PackageNameValidator().apply("org.lab.osm.."));
	}

	@Test
	public void testKo03() {
		Assert.assertFalse(new PackageNameValidator().apply("org..lab.osm"));
	}

	@Test
	public void testKo04() {
		Assert.assertFalse(new PackageNameValidator().apply("org.1.osm"));
	}

	@Test
	public void testNullKo01() {
		Assert.assertFalse(new PackageNameValidator().apply(null));
	}

	@Test
	public void testNullKo02() {
		Assert.assertFalse(new PackageNameValidator().apply(StringUtils.EMPTY));
	}

	@Test
	public void testNullKo03() {
		Assert.assertFalse(new PackageNameValidator().apply(" "));
	}

}
