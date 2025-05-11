import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User {
    private int id;
    private String username;
    private String fullName;
    private String role;

    public User(int id, String username, String fullName, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }
    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public static User logIn(String username, String password) {
        Connection conn = Database.getConnection();
        User user = null;

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String fullName = rs.getString("full_name");
                String role = rs.getString("role");
                return new User(id, username, fullName, role);
            } else {
                System.out.println("Invalid username or password.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return user;
    }
}

