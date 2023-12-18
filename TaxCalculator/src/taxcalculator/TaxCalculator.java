package taxcalculator; // Pindahkan ke package yang sesuai

import java.util.Scanner;

public class TaxCalculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Masukkan jumlah pendapatan Anda: ");
        double income = scanner.nextDouble();

        double tax = calculateTax(income);

        System.out.println("Jumlah pajak yang harus Anda bayar: $" + tax);

        scanner.close();
    }

    public static double calculateTax(double income) {
        double tax = 0;

        if (income <= 50000) {
            tax = income * 0.05; // Tarif pajak 5% untuk pendapatan <= $50,000
        } else if (income <= 100000) {
            tax = 2500 + (income - 50000) * 0.1; // Tarif pajak 10% untuk pendapatan > $50,000 dan <= $100,000
        } else {
            tax = 7500 + (income - 100000) * 0.15; // Tarif pajak 15% untuk pendapatan > $100,000
        }

        return tax;
    }
}
