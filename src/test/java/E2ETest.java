import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
public class E2ETest {
    @Test
    void testReturnBookAndLeaveReviewFlow() {
        // Simulated reader input in console:
        String simulatedInput = "yes\n1\nyes\n5\nGreat book!\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Reader reader = new Reader(1, "test_user", "reader");
        // Call returnBook which triggers leaveReview
        reader.returnBook(new java.util.Scanner(System.in));
        String result = output.toString();

        assertTrue(result.contains("Please provide a rating"), "Should prompt for rating");
        assertTrue(result.contains("Please leave a review"), "Should prompt for review");
        assertTrue(result.contains("Your review and rating have been recorded"), "Should confirm saving");
    }
}
