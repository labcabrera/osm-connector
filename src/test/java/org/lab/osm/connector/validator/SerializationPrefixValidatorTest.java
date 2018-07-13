package org.lab.osm.connector.validator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class SerializationPrefixValidatorTest {

	@Test
	public void test_empty_ok_01() {
		Assert.assertTrue(new SerializationPrefixValidator().apply(null));
	}

	@Test
	public void test_empty_ok_02() {
		Assert.assertTrue(new SerializationPrefixValidator().apply(StringUtils.EMPTY));
	}

	@Test
	public void test_ok_01() {
		Assert.assertTrue(new SerializationPrefixValidator().apply("test"));
	}

	@Test
	public void test_ok_02() {
		Assert.assertTrue(new SerializationPrefixValidator().apply("test-x"));
	}

	@Test
	public void test_ok_03() {
		Assert.assertTrue(new SerializationPrefixValidator().apply("test-x-"));
	}

	@Test
	public void test_ko_01() {
		Assert.assertFalse(new SerializationPrefixValidator().apply("test/invalid"));
	}

	@Test
	public void test_ko_02() {
		Assert.assertFalse(new SerializationPrefixValidator().apply("test:invalid"));
	}

}
