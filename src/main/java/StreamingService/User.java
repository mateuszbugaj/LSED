package StreamingService;

public class User {
    private String name;
    private boolean admin = false;
    private boolean banned = false;

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isAdmin() {
        return admin;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", adminPrivileges=" + admin +
                ", banned=" + banned +
                '}';
    }
}
