package taxcalculator;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class TaxCalculatorTest {
    private static int checks;

    public static void main(String[] args) throws Exception {
        checkAdapter();
        checkConsole();
        System.out.println("TaxCalculatorTest: " + checks + " pemeriksaan lulus.");
    }

    private static void checkAdapter() {
        near(0, 0);
        near(-0.0, 0);
        near(40000, 2000);
        near(50000, 2500);
        near(75000, 5000);
        near(100000, 7500);
        near(120000, 10500);
        near(0.10, 0.005);
        near(50000.01, 2500.001);
        near(100000.01, 7500.0015);
        near(Double.MIN_VALUE, 0);
        near(Double.MAX_VALUE, Double.MAX_VALUE * 0.15);
        for (double income : new double[] {-0.01, -1, -Double.MAX_VALUE,
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            rejects(() -> TaxCalculator.calculateTax(income), "invalid double " + income);
        }
    }

    private static void checkConsole() throws Exception {
        String prompt = "Masukkan jumlah pendapatan Anda: ";
        String result = "Pajak pada skema ini: $";
        equal(prompt + result + "2500.00\n", console("50000"));
        equal(prompt + result + "10500.00\n", console("120000"));
        equal(prompt + result + "0.00\n", console("0"));
        equal(prompt + result + "0.00\n", console("0.01"));
        equal(prompt + result + "0.01\n", console("0.10"));
        equal(prompt + result + "2500.00\n", console("50000.01"));
        equal(prompt + result + "149999999999992500.00\n",
                console("999999999999999999.99"));
        equal(prompt + result + "0.50\n", console(" \n\t10.00  "));
        equal(prompt, console(""));
        equal(prompt, console(" \n\t"));

        String[] invalid = {"hello", "NaN", "Infinity", "-Infinity", "-1", "-0", "+1",
                "1e3", "1E999999999", "1,5", "1.234", "1.", ".5", "1_000",
                "1000000000000000000", "999999999999999999.999", "\u0661\u0662", repeat('9', 10000)};
        for (String token : invalid) {
            String output = console(token + " 120000");
            check(count(output, "Masukan tidak valid.") == 1, "error for " + token.substring(0, Math.min(24, token.length())));
            check(count(output, result) == 1 && output.endsWith(result + "10500.00\n"), "retry result");
        }
        String ended = console("invalid");
        check(ended.contains("Masukan tidak valid.") && !ended.contains(result), "EOF after error");

        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            equal(prompt + result + "0.01\n", console("0.10"));
        } finally {
            Locale.setDefault(original);
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(bytes, true, "UTF-8");
        Scanner input = new Scanner("10 20");
        TaxCalculator.run(input, output);
        check(input.hasNext() && "20".equals(input.next()), "borrowed scanner remains open and next token untouched");
        output.print("still open");
        check(new String(bytes.toByteArray(), StandardCharsets.UTF_8).endsWith("still open"), "borrowed output remains open");
        rejects(() -> TaxCalculator.run(null, output), "null scanner");
        rejects(() -> TaxCalculator.run(input, null), "null output");
        input.close();
        output.close();
    }

    private static String console(String value) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (Scanner input = new Scanner(value);
                PrintStream output = new PrintStream(bytes, true, "UTF-8")) {
            TaxCalculator.run(input, output);
        }
        return new String(bytes.toByteArray(), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    private static String repeat(char value, int count) {
        char[] chars = new char[count];
        java.util.Arrays.fill(chars, value);
        return new String(chars);
    }

    private static int count(String text, String needle) {
        int total = 0;
        for (int at = 0; (at = text.indexOf(needle, at)) >= 0; at += needle.length()) {
            total++;
        }
        return total;
    }

    private static void near(double income, double expected) {
        double actual = TaxCalculator.calculateTax(income);
        check(Double.isFinite(actual) && Math.abs(actual - expected) <= Math.max(1e-12, Math.abs(expected) * 1e-12),
                "tax for " + income + ": expected " + expected + ", got " + actual);
    }

    private static void equal(String expected, String actual) {
        check(expected.equals(actual), "expected <" + expected + ">, got <" + actual + ">");
    }

    private static void rejects(Runnable action, String label) {
        try {
            action.run();
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException: " + label);
    }

    private static void check(boolean passed, String label) {
        checks++;
        if (!passed) {
            throw new AssertionError(label);
        }
    }
}
