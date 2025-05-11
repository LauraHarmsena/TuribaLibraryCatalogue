import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Librarian extends User {

    public Librarian(int id, String username, String role) {
        super(id, username, null, role);
    }
    public void addBook(Scanner scanner) {
        System.out.println("Enter required details to add a book to the catalogue:");
        System.out.print("Title (max 100 characters): ");
        String title = scanner.nextLine();
        if (title.length() > 100 || title.isEmpty()) {
            System.out.println("Invalid title. It must be between 1 and 100 characters.");
            return;
        }

        System.out.print("Author (max 30 characters): ");
        String author = scanner.nextLine();
        if (author.length() > 30 || author.isEmpty()) {
            System.out.println("Invalid author name. It must be between 1 and 30 characters.");
            return;
        }

        System.out.print("Genre (max 30 characters, starts with capital letter): ");
        String genre = scanner.nextLine();
        if (genre.length() > 30 || genre.isEmpty() || !Character.isUpperCase(genre.charAt(0))) {
            System.out.println("Invalid genre. It must start with a capital letter and be between 1 and 30 characters.");
            return;
        }

        System.out.print("Year (yyyy): ");
        String yearStr = scanner.nextLine();
        if (!yearStr.matches("\\d{4}")) {
            System.out.println("Invalid year. It must be exactly 4 digits.");
            return;
        }
        int year = Integer.parseInt(yearStr);

        System.out.print("Keyword (max 30 characters): ");
        String keywords = scanner.nextLine();
        if (keywords.length() > 30 || keywords.isEmpty()) {
            System.out.println("Invalid keyword. It must be between 1 and 30 characters.");
            return;
        }

        Connection conn = Database.getConnection();
        String sql = "INSERT INTO books (title, author, genre, year, keywords) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, author);
            pstmt.setString(3, genre);
            pstmt.setInt(4, year);
            pstmt.setString(5, keywords);

            pstmt.executeUpdate();
            System.out.println("Book added successfully: " + title);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteBook(Scanner scanner) {
        Connection conn = Database.getConnection();
        List<Integer> bookIds = new ArrayList<>();
        List<String> bookTitles = new ArrayList<>();

        System.out.print("Enter the title of the book you want to delete: ");
        String searchTerm = scanner.nextLine();
        String sql = "SELECT * FROM books WHERE title LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nMatching Books:");
            int index = 1;
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");

                bookIds.add(id);
                bookTitles.add(title);

                System.out.println(index + ". " + title + " by " + author);
                index++;
            }

            if (bookIds.isEmpty()) {
                System.out.println("No matching books found.");
                return;
            }

            System.out.print("Enter the number of the book you want to delete: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > bookIds.size()) {
                    System.out.println("Invalid choice. Deletion cancelled.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                return;
            }

            int selectedBookId = bookIds.get(choice - 1);
            String selectedBookTitle = bookTitles.get(choice - 1);

            System.out.print("Are you sure you want to delete \"" + selectedBookTitle + "\"? (yes/no): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();

            if (!confirmation.equals("yes")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            String deleteSql = "DELETE FROM books WHERE id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, selectedBookId);
                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Book successfully deleted.");
                } else {
                    System.out.println("Failed to delete the book.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error deleting book: " + e.getMessage());
        }
    }

    public void viewAllBooks() {
        Connection conn = Database.getConnection();
        String sql = "SELECT * FROM books";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nAll Books in the Catalogue:");

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");
                int year = rs.getInt("year");
                String keywords = rs.getString("keywords");

                System.out.printf("ID: %d | Title: %s | Author: %s | Genre: %s | Year: %d | Keywords: %s%n",
                        id, title, author, genre, year, keywords);
            }
        } catch (SQLException e) {
            System.out.println("Error displaying books: " + e.getMessage());
        }
    }
    public void modifyReviews(Scanner scanner) {
        Connection conn = Database.getConnection();

        String sql = "SELECT r.id, b.title, r.user_name, r.rating, r.comment, r.review_date " +
                "FROM reviews r JOIN books b ON r.book_id = b.id";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Integer> reviewIds = new ArrayList<>();
            int index = 1;

            System.out.println("--- All Reviews ---");
            while (rs.next()) {
                int reviewId = rs.getInt("id");
                String title = rs.getString("title");
                String username = rs.getString("user_name");
                int rating = rs.getInt("rating");
                String comment = rs.getString("comment");
                String date = rs.getString("review_date");

                System.out.printf("%d. Book: %s | By: %s | Rating: %d/5 | Date: %s\n", index, title, username, rating, date);
                System.out.println("   Comment: " + comment);
                System.out.println("-------------------------------");

                reviewIds.add(reviewId);
                index++;
            }

            if (reviewIds.isEmpty()) {
                System.out.println("There are no reviews to modify.");
                return;
            }

            System.out.print("Enter the number of the review you want to delete (or press Enter to cancel): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Operation cancelled.");
                return;
            }

            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= reviewIds.size()) {
                    int reviewIdToDelete = reviewIds.get(choice - 1);
                    deleteReviewById(conn, reviewIdToDelete);
                } else {
                    System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving reviews: " + e.getMessage());
        }
    }

    private void deleteReviewById(Connection conn, int reviewId) {
        String deleteSql = "DELETE FROM reviews WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(deleteSql)) {
            stmt.setInt(1, reviewId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Review successfully deleted.");
            } else {
                System.out.println("Failed to delete the review.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting review: " + e.getMessage());
        }
    }
    public void forceReturnBook(Scanner scanner) {
        Connection conn = Database.getConnection();
        List<Integer> borrowedIds = new ArrayList<>();

        String sql = "SELECT bb.id, b.title, b.author, bb.user_name, bb.borrow_date, bb.due_date " +
                "FROM borrowed_books bb " +
                "JOIN books b ON bb.book_id = b.id " +
                "WHERE bb.return_date IS NULL";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int index = 1;
            System.out.println("\nBorrowed Books");

            while (rs.next()) {
                int borrowedId = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String user = rs.getString("user_name");
                String borrowDate = rs.getString("borrow_date");
                String dueDate = rs.getString("due_date");

                borrowedIds.add(borrowedId);
                System.out.printf("%d. %s by %s | Borrowed by: %s | Borrowed on: %s | Due: %s%n",
                        index, title, author, user, borrowDate, dueDate);
                index++;
            }

            if (borrowedIds.isEmpty()) {
                System.out.println("No books to return.");
                return;
            }

            System.out.print("Enter the number of the book to force return: ");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice < 1 || choice > borrowedIds.size()) {
                System.out.println("Try again!");
                return;
            }

            int selectedBorrowedId = borrowedIds.get(choice - 1);
            String updateSql = "UPDATE borrowed_books SET return_date = ? WHERE id = ?";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String today = LocalDate.now().format(formatter);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, today);
                updateStmt.setInt(2, selectedBorrowedId);
                updateStmt.executeUpdate();
                System.out.println("Book successfully marked as returned.");
            }

        } catch (Exception e) {
            System.out.println("Error forcing return: " + e.getMessage());
        }
    }
}
