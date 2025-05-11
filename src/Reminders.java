import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Reminders {
    public static void sendDueReminders() {
        Connection conn = Database.getConnection();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String todayStr = today.format(formatter);
        String sql = """
            SELECT bb.book_id, b.title, u.username, u.email, bb.due_date
            FROM borrowed_books bb
            JOIN users u ON bb.user_name = u.username
            JOIN books b ON bb.book_id = b.id
            WHERE bb.return_date IS NULL AND bb.due_date = ?
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)){
             pstmt.setString(1, todayStr);
             ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String title = rs.getString("title");
                String email = rs.getString("email");
                String username = rs.getString("username");
                String dueDate = rs.getString("due_date");

                String subject = "Reminder: Book due today!";
                String message = "Dear " + username + ",\n\n" +
                        "This is a reminder that the book \"" + title + "\" is due today (" + dueDate + ").\n" +
                        "Please return it as soon as possible.\n\nThank you!\nOnline Library";

                EmailSender.sendEmail(email, subject, message);
            }
        } catch (SQLException e) {
            System.out.println("Error sending reminders: " + e.getMessage());
        }
    }
}

