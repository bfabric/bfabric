package org.bfabric.it;

import java.math.BigDecimal;

import org.bfabric.util.NumberUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberRoundingTest {

    public String round(String value) {
        return NumberUtils.getRoundedPrice(new BigDecimal(value), "EUR").toString();
    }

    @Test
    public void round() {
        assertEquals("0.00", round("0.00"));
        assertEquals("0.01", round("0.01"));
        assertEquals("0.02", round("0.02"));
        assertEquals("0.03", round("0.03"));
        assertEquals("0.04", round("0.04"));
        assertEquals("0.05", round("0.05"));
        assertEquals("0.06", round("0.06"));
        assertEquals("0.07", round("0.07"));
        assertEquals("0.08", round("0.08"));
        assertEquals("0.09", round("0.09"));
        assertEquals("0.10", round("0.1"));

        assertEquals("0.00", round("0.004"));
        assertEquals("0.01", round("0.005"));
        assertEquals("0.01", round("0.006"));
        assertEquals("0.02", round("0.015"));
        assertEquals("0.03", round("0.025"));
        assertEquals("0.04", round("0.035"));
        assertEquals("0.05", round("0.045"));
        assertEquals("0.06", round("0.055"));
        assertEquals("0.07", round("0.065"));
        assertEquals("0.08", round("0.075"));
        assertEquals("0.09", round("0.085"));
        assertEquals("0.10", round("0.095"));
    }

    public String roundCHF(String value) {
        return NumberUtils.getRoundedPrice(new BigDecimal(value), "CHF").toString();
    }

    @Test
    public void roundCHF() {
        assertEquals("0.00", roundCHF("0.00"));
        assertEquals("0.00", roundCHF("0.01"));
        assertEquals("0.00", roundCHF("0.02"));
        assertEquals("0.05", roundCHF("0.03"));
        assertEquals("0.05", roundCHF("0.04"));
        assertEquals("0.05", roundCHF("0.05"));
        assertEquals("0.05", roundCHF("0.06"));
        assertEquals("0.05", roundCHF("0.07"));
        assertEquals("0.10", roundCHF("0.08"));
        assertEquals("0.10", roundCHF("0.09"));

        assertEquals("0.00", roundCHF("0.005"));
        assertEquals("0.00", roundCHF("0.015"));
        assertEquals("0.05", roundCHF("0.025"));
        assertEquals("0.05", roundCHF("0.035"));
        assertEquals("0.05", roundCHF("0.045"));
        assertEquals("0.05", roundCHF("0.055"));
        assertEquals("0.05", roundCHF("0.065"));
        assertEquals("0.10", roundCHF("0.075"));
        assertEquals("0.10", roundCHF("0.085"));
        assertEquals("0.10", roundCHF("0.095"));
    }
}