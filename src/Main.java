import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Database.connect();
        Scanner scanner = new Scanner(System.in);
        Menu menu = new Menu(scanner);
        User user = null;
        final int MAX_ATTEMPTS = 3;
        int attempts = 0;

        System.out.println("Welcome to the Library Catalogue System!");
        while (attempts < MAX_ATTEMPTS && user == null) {
            System.out.println("Please log in!");

            System.out.print("Username: ");
            String username = scanner.nextLine();

            System.out.print("Password: ");
            String password = scanner.nextLine();

            user = User.logIn(username, password);

            if (user == null) {
                attempts++;
                System.out.println("Login failed. Please try again!");

                if (attempts == MAX_ATTEMPTS) {
                    System.out.println("Login failed. The system is shutting down.");
                    Database.disconnect();
                    scanner.close();
                    System.exit(0);
                }
            }
        }

        if (user != null) {
            System.out.println("Welcome, " + user.getFullName() + "!");
            if (user.getRole().equals("librarian")) {
                System.out.println("You are logged in as Librarian.");
                Librarian librarian = new Librarian(user.getId(), user.getUsername(), user.getRole());
                menu.librarianMenu(librarian);

            } else if (user.getRole().equals("reader")) {
                System.out.println("You are logged in as Reader.");
                Reader reader = new Reader(user.getId(), user.getUsername(), user.getRole());
                menu.readerMenu(reader);
            }
        }
        Database.disconnect();
        scanner.close();
    }
}