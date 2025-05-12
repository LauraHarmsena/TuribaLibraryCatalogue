import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MenuTest {
    @Test
    void testInvalidMenuInput() {
        Scanner testScanner = new Scanner("invalid\n9\n");
        Menu menu = new Menu(testScanner);
        Reader dummyReader = new Reader(1, "testuser", "reader");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        menu.readerMenu(dummyReader);
        String consoleOutput = output.toString();
        assertTrue(consoleOutput.contains("Invalid choice. Please try again."),
                "Expected error message not found in output for invalid menu input.");
    }
}