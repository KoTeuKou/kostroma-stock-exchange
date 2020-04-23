package edu.students.kse.me;

import org.junit.Assert;

import java.math.BigDecimal;

public class TestUtils {

    public static void assertEquals(String msg, BigDecimal expected, BigDecimal actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null || actual == null) {
            Assert.fail(msg + " Expected [" + expected + "], Actual [" + actual +  "]");
        }
        Assert.assertTrue(msg + " Expected [" + expected + "], Actual [" + actual +  "]", expected.compareTo(actual) == 0);
    }

    public static void assertEquals(final BigDecimal one, final BigDecimal two) {
        assertEquals("", one, two);
    }
}
