public class EmailSender {
    public static void sendEmail(String to, String subject, String message) {

        System.out.println("\nSimulated email");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Message:\n" + message);
    }
}

