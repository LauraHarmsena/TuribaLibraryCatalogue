import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testLoginWithInvalidCredentials() {
        // wrong username or password
        User user = User.logIn("wrongUsername", "wrongPassword");
        assertNull(user, "Login should fail and return null with invalid credentials.");
    }

    @Test
    public void testLoginWithValidCredentials() {
        // with actual credentials
        User user = User.logIn("reader1", "password111");
        assertNotNull(user, "Login should succeed with valid credentials.");
        assertEquals("reader1", user.getUsername());
    }

}
