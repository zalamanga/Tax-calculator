package taxcalculator;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;
import java.util.regex.Pattern;

public class TaxCalculator {
    private static final Pattern INCOME = Pattern.compile("[0-9]{1,18}(?:\\.[0-9]{1,2})?");
    private static final String INVALID_INPUT =
            "Masukan tidak valid. Gunakan angka 0 sampai 999999999999999999.99 "
            + "dengan maksimal 2 angka desimal (titik).";

    public static void main(String[] args) {
        run(new Scanner(System.in), System.out);
    }

    public static void run(Scanner input, PrintStream output) {
        if (input == null || output == null) {
            throw new IllegalArgumentException("Input dan output harus tersedia.");
        }

        while (true) {
            output.print("Masukkan jumlah pendapatan Anda: ");
            output.flush();
            if (!input.hasNext()) {
                return;
            }

            String value = input.next();
            if (value.length() > 21 || !INCOME.matcher(value).matches()) {
                output.println(INVALID_INPUT);
                continue;
            }

            BigDecimal tax = TaxRules.calculateTax(new BigDecimal(value));
            output.println("Pajak pada skema ini: $"
                    + tax.setScale(2, RoundingMode.HALF_UP).toPlainString());
            return;
        }
    }

    public static double calculateTax(double income) {
        if (Double.isNaN(income) || Double.isInfinite(income) || income < 0) {
            throw new IllegalArgumentException("Pendapatan harus berupa angka nonnegatif yang terbatas.");
        }
        return TaxRules.calculateTax(BigDecimal.valueOf(income)).doubleValue();
    }
}
