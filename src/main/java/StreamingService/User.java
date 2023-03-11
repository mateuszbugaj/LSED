package StreamingService;

public class User {
    private String name;
    private boolean adminPrivileges = false;
    private boolean banned = false;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void giveAdminPrivileges() {
        adminPrivileges = true;
    }

    public boolean hasAdminPrivileges() {
        return adminPrivileges;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }
}
