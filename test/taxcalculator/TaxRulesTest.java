package taxcalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TaxRulesTest {
    private static int checks;

    public static void main(String[] args) {
        verifyExactTaxFixtures();
        verifyDisplayRoundingKeepsExactResult();
        verifyInvalidInputs();
        System.out.println("TaxRulesTest: " + checks + " checks passed.");
    }

    private static void verifyExactTaxFixtures() {
        expectTax("0", "0");
        expectTax("0.00", "0");
        expectTax("0.01", "0.0005");
        expectTax("0.10", "0.005");
        expectTax("1", "0.05");
        expectTax("40000", "2000");
        expectTax("49999.99", "2499.9995");
        expectTax("50000", "2500");
        expectTax("50000.00", "2500");
        expectTax("50000.01", "2500.001");
        expectTax("75000", "5000");
        expectTax("99999.99", "7499.999");
        expectTax("100000", "7500");
        expectTax("100000.00", "7500");
        expectTax("100000.01", "7500.0015");
        expectTax("120000", "10500");
        expectTax("150000", "15000");
        expectTax("999999999999999999.99", "149999999999992499.9985");

        // The calculation API accepts precision and amounts beyond the CLI input limits.
        expectTax("0.00000000000000000001", "0.0000000000000000000005");
        expectTax("50000.000001", "2500.0000001");
        expectTax("100000.000001", "7500.00000015");
        expectTax("1000000000000000000000000000000", "149999999999999999999999992500");
    }

    private static void verifyDisplayRoundingKeepsExactResult() {
        BigDecimal exact = TaxRules.calculateTax(new BigDecimal("0.10"));
        check(exact.compareTo(new BigDecimal("0.005")) == 0, "half-cent remains exact");
        check(exact.setScale(2, RoundingMode.HALF_UP).toPlainString().equals("0.01"), "half-cent display rounds up");
        check(TaxRules.calculateTax(new BigDecimal("0.09")).setScale(2, RoundingMode.HALF_UP)
                .toPlainString().equals("0.00"), "below half-cent display rounds down");
        check(TaxRules.calculateTax(new BigDecimal("49999.99")).setScale(2, RoundingMode.HALF_UP)
                .toPlainString().equals("2500.00"), "display rounding near bracket boundary");
    }

    private static void verifyInvalidInputs() {
        expectInvalid(null);
        expectInvalid(new BigDecimal("-0.01"));
        expectInvalid(new BigDecimal("-1"));
        expectInvalid(new BigDecimal("-1000000000000000000000000000000"));
        expectInvalid(new BigDecimal("-0.00000000000000000000001"));
    }

    private static void expectTax(String income, String expected) {
        BigDecimal actual = TaxRules.calculateTax(new BigDecimal(income));
        check(actual.compareTo(new BigDecimal(expected)) == 0, "exact tax for " + income);
    }

    private static void expectInvalid(BigDecimal income) {
        try {
            TaxRules.calculateTax(income);
            throw new AssertionError("Expected invalid income to be rejected");
        } catch (IllegalArgumentException expected) {
            check(expected.getMessage() != null && !expected.getMessage().isEmpty(), "invalid income has an explanation");
        }
    }

    private static void check(boolean condition, String description) {
        if (!condition) {
            throw new AssertionError(description);
        }
        checks++;
    }
}
