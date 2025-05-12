import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ReviewManager {

    private static final int MAX_REVIEW_LENGTH = 100;

    public void leaveReview(Scanner scanner, String username, int bookId, String bookTitle) {
        Connection conn = Database.getConnection();

        try {
            int rating = 0;
            while (true) {
                System.out.print("Please provide a rating for this book (1-5): ");
                try {
                    rating = Integer.parseInt(scanner.nextLine().trim());
                    if (rating >= 1 && rating <= 5) {
                        break;
                    } else {
                        System.out.println("Please enter a number between 1 and 5!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Please enter a number between 1 and 5!");
                }
            }

            String comment = "";
            while (true) {
                System.out.print("Please leave a review (max 100 characters): ");
                comment = scanner.nextLine().trim();

                if (comment.length() > MAX_REVIEW_LENGTH) {
                    System.out.println("Your review is too long. Please limit it to 100 characters.");
                } else if (comment.isEmpty()) {
                    System.out.println("Please leave a review to compliment your rating!");
                } else {
                    break;
                }
            }

            String insertSql = "INSERT INTO reviews (book_id, user_name, rating, comment, review_date) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setInt(1, bookId);
            insertStmt.setString(2, username);
            insertStmt.setInt(3, rating);
            insertStmt.setString(4, comment);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String formattedDate = LocalDate.now().format(formatter);
            insertStmt.setString(5, formattedDate);

            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Thank you! Your review and rating have been recorded.");
            } else {
                System.out.println("Failed to submit your review. Please try again.");
            }

        } catch (Exception e) {
            System.out.println("Error submitting review: " + e.getMessage());
        }
    }

    public void viewReviews(int bookId, String bookTitle) {
        Connection conn = Database.getConnection();
        System.out.println("Reviews for: " + bookTitle);
        String sql = "SELECT user_name, rating, comment, review_date FROM reviews WHERE book_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            boolean reviewsFound = false;
            while (rs.next()) {
                reviewsFound = true;
                String userName = rs.getString("user_name");
                int rating = rs.getInt("rating");
                String comment = rs.getString("comment");
                String reviewDate = rs.getString("review_date");

                System.out.println("Reviewer: " + userName);
                System.out.println("Rating: " + rating + "/5");
                System.out.println("Comment: " + comment);
                System.out.println("Date: " + reviewDate);
                System.out.println("-----------------------------");
            }

            if (!reviewsFound) {
                System.out.println("No reviews found for this book.");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving reviews: " + e.getMessage());
        }
    }
    public void viewTopRatedBooks(Scanner scanner) {
        Connection conn = Database.getConnection();
        String sql = "SELECT b.id, b.title, ROUND(AVG(r.rating), 2) as avg_rating " +
                "FROM reviews r JOIN books b ON r.book_id = b.id " +
                "GROUP BY r.book_id ORDER BY avg_rating DESC";

        List<Integer> bookIds = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Best rated books (5-1)");
            int index = 1;
            while (rs.next()) {
                int bookId = rs.getInt("id");
                String title = rs.getString("title");
                double avgRating = rs.getDouble("avg_rating");

                System.out.printf("%d. %s (%.2f)\n", index, title, avgRating);
                bookIds.add(bookId);
                index++;
            }

            System.out.print("Enter the number of the book to view reviews: ");
            int choice = Integer.parseInt(scanner.nextLine());
            if (choice >= 1 && choice <= bookIds.size()) {
                int selectedBookId = bookIds.get(choice - 1);
                String selectedBookTitle = getBookTitleById(selectedBookId);
                viewReviews(selectedBookId, selectedBookTitle);
            } else {
                System.out.println("Invalid choice.");
            }

        } catch (SQLException e) {
            System.out.println("Error loading top-rated books: " + e.getMessage());
        }
    }
    public int searchBookByTitle(Scanner scanner, String searchTerm) {
        Connection conn = Database.getConnection();
        int foundBookId = -1;

        String sql = "SELECT id, title FROM books WHERE title LIKE ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + searchTerm + "%");
            ResultSet rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("No books found matching such title.");
                return -1;
            }

            System.out.println("Matching books:");
            int index = 1;
            Map<Integer, Integer> bookMap = new HashMap<>();

            do {
                int bookId = rs.getInt("id");
                String title = rs.getString("title");

                bookMap.put(index, bookId);
                System.out.println(index + ". " + title);
                index++;
            } while (rs.next());

            System.out.print("Enter the number of the book you want to review: ");
            int choice;

            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (bookMap.containsKey(choice)) {
                    foundBookId = bookMap.get(choice);
                } else {
                    System.out.println("Invalid number. Operation cancelled.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Operation cancelled.");
            }

        } catch (SQLException e) {
            System.out.println("Error searching for books: " + e.getMessage());
        }
        return foundBookId;
    }
    public String getBookTitleById(int bookId) {
        Connection conn = Database.getConnection();
        String bookTitle = null;

        String sql = "SELECT title FROM books WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                bookTitle = rs.getString("title");
            } else {
                System.out.println("Book not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving book title: " + e.getMessage());
        }
        return bookTitle;
    }
}
