package ead_cw.session;

/**
 * Stores the currently logged-in user while the application is running.
 */
public final class UserSession {

    private static int userId;
    private static String username;
    private static String role;
    private static Integer veterinarianId;

    private UserSession() {
    }

    public static void login(int loggedUserId, String loggedUsername,
            String loggedRole, Integer linkedVeterinarianId) {
        userId = loggedUserId;
        username = loggedUsername;
        role = loggedRole;
        veterinarianId = linkedVeterinarianId;
    }

    public static void logout() {
        userId = 0;
        username = null;
        role = null;
        veterinarianId = null;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getUsername() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static Integer getVeterinarianId() {
        return veterinarianId;
    }

    public static boolean hasRole(String requiredRole) {
        return requiredRole != null && requiredRole.equals(role);
    }
}
