package Devices;

import java.util.Date;

public class ReceivedMessage {
    private final String deviceName;
    private final String content;
    private final Date timestamp;

    public ReceivedMessage(String deviceName, String message, Date timestamp) {
        this.deviceName = deviceName;
        this.content = message;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getDeviceName() {
        return deviceName;
    }
}
