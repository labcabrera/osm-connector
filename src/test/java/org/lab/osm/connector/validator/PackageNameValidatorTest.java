package org.lab.osm.connector.validator;

import org.junit.Assert;
import org.junit.Test;
import org.lab.osm.connector.validator.PackageNameValidator;

public class PackageNameValidatorTest {

	@Test
	public void test_ok_01() {
		Assert.assertTrue(new PackageNameValidator().apply("org.lab.osm"));
	}

	@Test
	public void test_ok_02() {
		Assert.assertTrue(new PackageNameValidator().apply("org.lab.osm"));
	}

	@Test
	public void test_ok_03() {
		Assert.assertTrue(new PackageNameValidator().apply("org.lab21.osm42"));
	}

	@Test
	public void test_ok_04() {
		Assert.assertTrue(new PackageNameValidator().apply("a.b.c"));
	}

	@Test
	public void test_ok_05() {
		Assert.assertTrue(new PackageNameValidator().apply("package"));
	}

	@Test
	public void test_ko_01() {
		Assert.assertFalse(new PackageNameValidator().apply(".org.lab.osm"));
	}

	@Test
	public void test_ko_02() {
		Assert.assertFalse(new PackageNameValidator().apply("org.lab.osm.."));
	}

	@Test
	public void test_ko_03() {
		Assert.assertFalse(new PackageNameValidator().apply("org..lab.osm"));
	}

	@Test
	public void test_ko_04() {
		Assert.assertFalse(new PackageNameValidator().apply("org.1.osm"));
	}

}
