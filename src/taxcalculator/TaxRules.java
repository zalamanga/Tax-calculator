package taxcalculator;

import java.math.BigDecimal;

public final class TaxRules {
    private static final BigDecimal FIRST_LIMIT = new BigDecimal("50000");
    private static final BigDecimal SECOND_LIMIT = new BigDecimal("100000");
    private static final BigDecimal FIRST_RATE = new BigDecimal("0.05");
    private static final BigDecimal SECOND_RATE = new BigDecimal("0.10");
    private static final BigDecimal THIRD_RATE = new BigDecimal("0.15");
    private static final BigDecimal FIRST_BRACKET_TAX = new BigDecimal("2500");
    private static final BigDecimal SECOND_BRACKET_TAX = new BigDecimal("7500");

    private TaxRules() {
    }

    /** Applies the project's educational rates without rounding the result. */
    public static BigDecimal calculateTax(BigDecimal income) {
        if (income == null) {
            throw new IllegalArgumentException("Income must not be null.");
        }
        if (income.signum() < 0) {
            throw new IllegalArgumentException("Income cannot be negative.");
        }
        if (income.compareTo(FIRST_LIMIT) <= 0) {
            return income.multiply(FIRST_RATE);
        }
        if (income.compareTo(SECOND_LIMIT) <= 0) {
            return FIRST_BRACKET_TAX.add(income.subtract(FIRST_LIMIT).multiply(SECOND_RATE));
        }
        return SECOND_BRACKET_TAX.add(income.subtract(SECOND_LIMIT).multiply(THIRD_RATE));
    }
}
