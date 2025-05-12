import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Reader extends User {
    public Reader(int id, String username, String role) {
        super(id, username, null, role);
    }
    private ReviewManager reviewManager = new ReviewManager();

    public void viewAllBooks() {
        Connection conn = Database.getConnection();
        String sql = "SELECT * FROM books";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            System.out.println("All books:");
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String genre = rs.getString("genre");
                int year = rs.getInt("year");

                System.out.println(id + ": " + title + " by " + author + " (" + genre + ", " + year + ")");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void borrowBook(int bookId, Scanner scanner) {
        Connection conn = Database.getConnection();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String checkSql = "SELECT * FROM borrowed_books WHERE book_id = ? AND return_date IS NULL";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("This book is already borrowed by someone else.");
                System.out.print("Would you like to reserve this book? (yes/no): ");
                String response = scanner.nextLine().trim().toLowerCase();

                if (response.equals("yes")) {
                    reserveBook(scanner, bookId);
                } else {
                    System.out.println("Returning to menu.");
                }
                return;
            }

            String insertSql = "INSERT INTO borrowed_books (book_id, user_name, borrow_date, due_date) VALUES (?, ?, ?, ?)";

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, bookId);
                insertStmt.setString(2, this.getUsername());

                LocalDate borrowDate = LocalDate.now();
                LocalDate dueDate = borrowDate.plusWeeks(2);

                String formattedBorrowDate = borrowDate.format(formatter);
                String formattedDueDate = dueDate.format(formatter);

                insertStmt.setString(3, formattedBorrowDate);
                insertStmt.setString(4, formattedDueDate);

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Book successfully borrowed! Due date: " + formattedDueDate);
                } else {
                    System.out.println("Failed to borrow the book.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error borrowing book: " + e.getMessage());
        }
    }

    public void returnBook(Scanner scanner) {
        Connection conn = Database.getConnection();
        List<Integer> borrowedBookIds = new ArrayList<>();
        List<Integer> bookIds = new ArrayList<>();
        List<String> bookTitles = new ArrayList<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String sql = "SELECT bb.id, bb.book_id, b.title, b.author " +
                "FROM borrowed_books bb " +
                "JOIN books b ON bb.book_id = b.id " +
                "WHERE bb.user_name = ? AND bb.return_date IS NULL";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.getUsername());
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nYour Borrowed Books");

            int index = 1;
            while (rs.next()) {
                int borrowedBookId = rs.getInt("id");
                int bookId = rs.getInt("book_id");
                String title = rs.getString("title");
                String author = rs.getString("author");

                borrowedBookIds.add(borrowedBookId);
                bookIds.add(bookId);
                bookTitles.add(title);

                System.out.printf("%d. %s by %s\n", index, title, author);
                index++;
            }

            if (borrowedBookIds.isEmpty()) {
                System.out.println("You have no borrowed books to return.");
                return;
            }

            System.out.print("Do you want to return a book? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (!response.equals("yes")) {
                System.out.println("Returning to menu.");
                return;
            }

            System.out.print("Enter the number of the book you want to return: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > borrowedBookIds.size()) {
                    System.out.println("Invalid number. Operation cancelled.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Operation cancelled.");
                return;
            }

            int selectedIndex = choice - 1;
            int selectedBorrowedBookId = borrowedBookIds.get(selectedIndex);
            int selectedBookId = bookIds.get(selectedIndex);
            String selectedBookTitle = bookTitles.get(selectedIndex);

            String updateSql = "UPDATE borrowed_books SET return_date = ? WHERE id = ?";

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                String formattedReturnDate = LocalDate.now().format(formatter);
                updateStmt.setString(1, formattedReturnDate);
                updateStmt.setInt(2, selectedBorrowedBookId);

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Book successfully returned.");
                    System.out.print("Would you like to leave a review for this book? (yes/no): ");
                    String reviewResponse = scanner.nextLine().trim().toLowerCase();

                    if (reviewResponse.equals("yes")) {
                        reviewManager.leaveReview(scanner, this.getUsername(), selectedBookId, selectedBookTitle);
                    }
                } else {
                    System.out.println("Failed to return the book.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error returning book: " + e.getMessage());
        }
    }

    public void reserveBook(Scanner scanner, int bookId) {
        Connection conn = Database.getConnection();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (bookId == -1) {
            System.out.print("Enter the ID of the book you want to reserve: ");
            bookId = scanner.nextInt();
            scanner.nextLine();
        }

        String checkSql = "SELECT due_date FROM borrowed_books WHERE book_id = ? AND return_date IS NULL";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, bookId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                String dueDateStr = rs.getString("due_date");
                LocalDate dueDate = LocalDate.parse(dueDateStr, formatter);
                LocalDate expectedAvailableDate = dueDate.plusDays(1);

                String formattedExpectedAvailableDate = expectedAvailableDate.format(formatter);

                String insertSql = "INSERT INTO reservations (book_id, user_name, reservation_date, expected_available_date) VALUES (?, ?, ?, ?)";

                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, bookId);
                    insertStmt.setString(2, this.getUsername());
                    insertStmt.setString(3, LocalDate.now().format(formatter));
                    insertStmt.setString(4, formattedExpectedAvailableDate);

                    int rowsAffected = insertStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Book successfully reserved! It will be available on " + formattedExpectedAvailableDate);
                    } else {
                        System.out.println("Failed to reserve the book.");
                    }
                }
            } else {
                System.out.println("This book is available, so you can borrow it directly.");
            }

        } catch (SQLException e) {
            System.out.println("Error reserving book: " + e.getMessage());
        }
    }

    public void viewAndCancelReservedBooks(Scanner scanner) {
        Connection conn = Database.getConnection();
        List<Integer> reservationIds = new ArrayList<>();

        String sql = "SELECT r.id, b.title, b.author, r.expected_available_date " +
                "FROM reservations r " +
                "JOIN books b ON r.book_id = b.id " +
                "WHERE r.user_name = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, this.getUsername());
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\nYour Reserved Books");
            int index = 1;

            while (rs.next()) {
                int reservationId = rs.getInt("id");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String expectedAvailableDate = rs.getString("expected_available_date");

                reservationIds.add(reservationId);

                System.out.printf("%d. %s by %s (Available on: %s)\n", index, title, author, expectedAvailableDate);
                index++;
            }

            if (reservationIds.isEmpty()) {
                System.out.println("You have no reserved books.");
                return;
            }

            System.out.print("Do you want to cancel a reservation? (yes/no): ");
            String response = scanner.nextLine().trim().toLowerCase();

            if (!response.equals("yes")) {
                System.out.println("Returning to menu.");
                return;
            }

            System.out.print("Enter the number of the reservation you want to cancel: ");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
                if (choice < 1 || choice > reservationIds.size()) {
                    System.out.println("Invalid number. Operation cancelled.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Operation cancelled.");
                return;
            }

            int selectedReservationId = reservationIds.get(choice - 1);

            String deleteSql = "DELETE FROM reservations WHERE id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, selectedReservationId);

                int rowsAffected = deleteStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Reservation successfully cancelled.");
                } else {
                    System.out.println("Failed to cancel the reservation.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving or cancelling reservations: " + e.getMessage());
        }
    }
}

