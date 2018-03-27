package org.aion.wallet.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * @author Cristian Ilca, Centrys Inc.
 */
public class BalanceFormatter {

    public static String formatBalance(BigInteger balance) {
        BigDecimal bigDecimalBalance = new BigDecimal(balance);
        BigDecimal decimalPlaces = new BigDecimal(BigInteger.valueOf(1000000000).multiply(BigInteger.valueOf(1000000000)));

        return String.valueOf(bigDecimalBalance.divide(decimalPlaces, 10, RoundingMode.HALF_EVEN));
    }
}
