import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Book {
    // Overloaded method for Librarian (no borrowing)
    public static void searchBooks(String searchTerm, Scanner scanner) {
        Connection conn = Database.getConnection();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR genre LIKE ? OR keywords LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcardTerm = "%" + searchTerm + "%";
            pstmt.setString(1, wildcardTerm);
            pstmt.setString(2, wildcardTerm);
            pstmt.setString(3, wildcardTerm);
            pstmt.setString(4, wildcardTerm);

            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nSearch Results for '" + searchTerm + "':");

            boolean found = false;
            int index = 1;
            while (rs.next()) {
                found = true;
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");
                int year = rs.getInt("year");

                System.out.printf("%d. %s by %s (Genre: %s, Year: %d)\n", index, title, author, genre, year);
                index++;
            }

            if (!found) {
                System.out.println("No books found matching your search.");
            }
        } catch (SQLException e) {
            System.out.println("Error searching books: " + e.getMessage());
        }
    }
    // Search books by reader
    public static void searchBooks(String searchTerm, Scanner scanner, Reader reader) {
        Connection conn = Database.getConnection();
        String sql = "SELECT * FROM books WHERE title LIKE ? OR author LIKE ? OR genre LIKE ? OR keywords LIKE ?";

        List<Integer> foundBookIds = new ArrayList<>();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String wildcardTerm = "%" + searchTerm + "%";
            pstmt.setString(1, wildcardTerm);
            pstmt.setString(2, wildcardTerm);
            pstmt.setString(3, wildcardTerm);
            pstmt.setString(4, wildcardTerm);

            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nSearch Results for '" + searchTerm + "':");

            boolean found = false;
            int index = 1;
            while (rs.next()) {
                found = true;
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");
                int year = rs.getInt("year");

                foundBookIds.add(id);
                System.out.printf("%d. %s by %s (Genre: %s, Year: %d)\n", index, title, author, genre, year);
                index++;
            }

            if (!found) {
                System.out.println("No books found matching your search.");
                return;
            }

            System.out.print("\nEnter the number of the book you want to borrow or press Enter to return to Menu: ");
            String input = scanner.nextLine();
            if (!input.isBlank()) {
                try {
                    int choice = Integer.parseInt(input);
                    if (choice >= 1 && choice <= foundBookIds.size()) {
                        int selectedBookId = foundBookIds.get(choice - 1);
                        reader.borrowBook(selectedBookId, scanner);
                    } else {
                        System.out.println("Invalid number. Returning to menu.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Returning to menu.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error searching books: " + e.getMessage());
        }
    }
}
