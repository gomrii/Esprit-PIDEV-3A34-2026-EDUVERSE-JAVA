package Utils;

import com.elearning.entity.User;

public class Session {
    public static String role;
    public static int userId;
    public static User user;

    public static void setSession(User u) {
        user = u;
        role = u.getRole();
        userId = u.getId();
    }

    public static void clear() {
        user = null;
        role = null;
        userId = 0;
    }
}
