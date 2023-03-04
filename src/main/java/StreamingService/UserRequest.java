package StreamingService;

public class UserRequest {
    private final User user;
    private final int timeSeconds;

    public UserRequest(User user, int timeSeconds) {
        this.user = user;
        this.timeSeconds = timeSeconds;
    }

    public User getUser() {
        return user;
    }

    public int getTimeSeconds() {
        return timeSeconds;
    }
}

