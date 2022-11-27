package Devices;

import java.util.Date;

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
