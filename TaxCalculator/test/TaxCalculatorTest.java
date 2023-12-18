import java.io.ByteArrayInputStream;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import taxcalculator.TaxCalculator;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.After;
import org.junit.Before;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class TaxCalculatorTest {
    
   private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(System.out);
        System.setErr(System.err);
    }

    @Test
    public void testMainMethodWithValidInput() {
        String input = "50000\n"; // Input valid, pendapatan $50,000
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        TaxCalculator.main(new String[]{});

        String expectedOutput = "Masukkan jumlah pendapatan Anda: Jumlah pajak yang harus Anda bayar: $2500.0" + System.lineSeparator();
        assertEquals(expectedOutput, outContent.toString());
    }



    
    @Test
    public void testCalculateTaxForIncomeBelow50000() {
        double income = 40000; // Pendapatan di bawah $50,000
        double expectedTax = 40000 * 0.05; // Tarif pajak 5%
        double calculatedTax = TaxCalculator.calculateTax(income);
        assertEquals(expectedTax, calculatedTax, 0.001);
    }

    @Test
    public void testCalculateTaxForIncomeBetween50000And100000() {
        double income = 75000; // Pendapatan antara $50,000 dan $100,000
        double expectedTax = 2500 + (75000 - 50000) * 0.1; // Tarif pajak 10%
        double calculatedTax = TaxCalculator.calculateTax(income);
        assertEquals(expectedTax, calculatedTax, 0.001);
    }

    @Test
    public void testCalculateTaxForIncomeAbove100000() {
        double income = 120000; // Pendapatan di atas $100,000
        double expectedTax = 7500 + (120000 - 100000) * 0.15; // Tarif pajak 15%
        double calculatedTax = TaxCalculator.calculateTax(income);
        assertEquals(expectedTax, calculatedTax, 0.001);
    }

    @Test
    public void testCalculateTaxForZeroIncome() {
        double income = 0; // Pendapatan nol
        double expectedTax = 0; // Tidak ada pajak yang harus dibayarkan
        double calculatedTax = TaxCalculator.calculateTax(income);
        assertEquals(expectedTax, calculatedTax, 0.001);
    } 
}
