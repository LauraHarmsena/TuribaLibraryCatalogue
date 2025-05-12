import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

public class RemindersTest {

    @Test
    void testReminderIsSent() {
        Connection conn = Database.getConnection();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        try {
            String insertUser = "INSERT OR IGNORE INTO users (username, password, role, full_name, email) VALUES ('test_user', 'test_password', 'reader', 'Test Test', 'test@test.com')";
            conn.prepareStatement(insertUser).executeUpdate();
            String insertBook = "INSERT OR IGNORE INTO books (id, title, author, genre, year) VALUES (9999, 'Reminder Test Book', 'Author', 'Test', 2025)";
            conn.prepareStatement(insertBook).executeUpdate();
            String insertBorrowed = """
                INSERT INTO borrowed_books (book_id, user_name, borrow_date, due_date)
                VALUES (9999, 'test_user', ?, ?)
            """;
            PreparedStatement stmt = conn.prepareStatement(insertBorrowed);
            stmt.setString(1, today);
            stmt.setString(2, today);
            stmt.executeUpdate();
            Reminders.sendDueReminders();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    @Test
    void testNoReminderSentWhenNoDueBooksToday() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        Reminders.sendDueReminders();
        String result = output.toString();
        // Assertion: Should NOT contain any email output
        assertFalse(result.contains("Simulated email"), "Reminder was sent even though no book is due today.");
    }
}