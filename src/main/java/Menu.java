import java.util.Scanner;

public class Menu {

    private Scanner scanner;

    public Menu(Scanner scanner) {
        this.scanner = scanner;
    }
    private ReviewManager reviewManager = new ReviewManager();

    public void librarianMenu(Librarian librarian) {
        String[] librarianMenuOptions = {
                "Librarian's menu:",
                "1. Add a book",
                "2. Delete a book",
                "3. Search / Display all books",
                "4. Modify reviews",
                "5. Force return books",
                "6. Send reminders (due date)",
                "7. Log out"
        };

        while (true) {
            for (String option : librarianMenuOptions) {
                System.out.println(option);
            }
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    librarian.addBook(scanner);
                    break;

                case "2":
                    librarian.deleteBook(scanner);
                    break;

                case "3":
                    System.out.print("Enter search word (leave blank to display all books): ");
                    String searchTerm = scanner.nextLine();

                    if (searchTerm.trim().isEmpty()) {
                        librarian.viewAllBooks();
                    } else {
                        Book.searchBooks(searchTerm, scanner);
                    }
                    break;

                case "4":
                    librarian.modifyReviews(scanner);
                    break;

                case "5":
                    librarian.forceReturnBook(scanner);
                    break;
                case "6":
                    Reminders.sendDueReminders();
                    break;
                case "7":
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Please choose 1-6. Try again!");
            }
        }
    }

    public void readerMenu(Reader reader) {
        String[] readerMenuOptions = {
                "Reader's menu:",
                "1. View all books",
                "2. Search and borrow books",
                "3. View borrowed books / Return books",
                "4. Reserve books",
                "5. View / Cancel reserved books",
                "6. Leave a review",
                "7. View reviews",
                "8. View best rated books",
                "9. Log out"
        };

        while (true) {
            for (String option : readerMenuOptions) {
                System.out.println(option);
            }
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    reader.viewAllBooks();
                    System.out.print("Enter the ID of the book you want to borrow or press Enter to return to Menu: ");
                    String input = scanner.nextLine();
                    if (!input.isBlank()) {
                        try {
                            int bookId = Integer.parseInt(input);
                            reader.borrowBook(bookId, scanner);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input. Returning to menu.");
                        }
                    }
                    break;

                case "2":
                    System.out.print("Enter search word (leave blank to display all books): ");
                    String searchTerm = scanner.nextLine();

                    if (searchTerm.trim().isEmpty()) {
                        reader.viewAllBooks();
                    } else {
                        Book.searchBooks(searchTerm, scanner, reader);
                    }
                    break;

                case "3":
                    reader.returnBook(scanner);
                    break;

                case "4":
                    System.out.print("Enter the ID of the book you want to reserve: ");
                    int bookId = Integer.parseInt(scanner.nextLine());
                    reader.reserveBook(scanner, bookId);
                    break;

                case "5":
                    reader.viewAndCancelReservedBooks(scanner);
                    break;

                case "6":
                    System.out.print("Enter the title of the book you want to review (or part of it): ");
                    String reviewSearchTerm = scanner.nextLine();

                    int reviewBookId = reviewManager.searchBookByTitle(scanner, reviewSearchTerm);
                    if (reviewBookId != -1) {
                        String reviewBookTitle = reviewManager.getBookTitleById(reviewBookId);
                        if (reviewBookTitle != null) {
                            reviewManager.leaveReview(scanner, reader.getUsername(), reviewBookId, reviewBookTitle);
                        }
                    }
                    break;

                case "7":
                    System.out.print("Enter part of the book title to view reviews (minimum 4 characters): ");
                    String searchTitleForReview = scanner.nextLine();

                    if (searchTitleForReview.length() >= 4) {
                        int foundReviewBookId = reviewManager.searchBookByTitle(scanner, searchTitleForReview);

                        if (foundReviewBookId != -1) {
                            String foundReviewBookTitle = reviewManager.getBookTitleById(foundReviewBookId);

                            if (foundReviewBookTitle != null) {
                                reviewManager.viewReviews(foundReviewBookId, foundReviewBookTitle);
                            } else {
                                System.out.println("No matching book found.");
                            }
                        } else {
                            System.out.println("No matching book found.");
                        }
                    } else {
                        System.out.println("Search word must be at least 4 characters long.");
                    }
                    break;
                case "8":
                    reviewManager.viewTopRatedBooks(scanner);
                    break;
                case "9":
                    System.out.println("Logging out...");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
