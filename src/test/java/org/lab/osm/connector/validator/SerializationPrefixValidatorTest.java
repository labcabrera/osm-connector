package org.lab.osm.connector.validator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

public class SerializationPrefixValidatorTest {

	@Test
	public void testNullOk01() {
		Assert.assertTrue(new SerializationPrefixValidator().apply(null));
	}

	@Test
	public void testNullOk02() {
		Assert.assertTrue(new SerializationPrefixValidator().apply(StringUtils.EMPTY));
	}

	@Test
	public void testOk01() {
		Assert.assertTrue(new SerializationPrefixValidator().apply("test"));
	}

	@Test
	public void testOk02() {
		Assert.assertTrue(new SerializationPrefixValidator().apply("test-x"));
	}

	@Test
	public void testOk03() {
		Assert.assertTrue(new SerializationPrefixValidator().apply("test-x-"));
	}

	@Test
	public void testKo01() {
		Assert.assertFalse(new SerializationPrefixValidator().apply("test/invalid"));
	}

	@Test
	public void testKo02() {
		Assert.assertFalse(new SerializationPrefixValidator().apply("test:invalid"));
	}

}
