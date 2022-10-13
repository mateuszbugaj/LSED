package Device;

import java.util.Date;

// todo: get better name to better indicate that there is more metadata than just a message
public class ReceivedMessage {
    private final String message;
    private final Date timestamp;

    public ReceivedMessage(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }
}
