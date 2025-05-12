import org.junit.jupiter.api.Test;
import java.util.Scanner;

public class ReviewManagerTest {

    @Test
    public void testLeaveReview_AllStatementsCovered() {
        ReviewManager reviewManager = new ReviewManager();
        Scanner scanner = new Scanner("5\nGreat book!\n");

        String username = "testuser";
        int bookId = 1;
        String bookTitle = "Test Book";

        reviewManager.leaveReview(scanner, username, bookId, bookTitle);
    }
}