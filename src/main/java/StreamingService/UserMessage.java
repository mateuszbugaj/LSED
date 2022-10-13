package StreamingService;

import Device.Device;

import java.util.Date;

public class UserMessage {
    private final String user; // todo: this will be it's own class with properties and privileges read from saved file in DB
    private final String content;
    private final Date timestamp;
    private Device targetDevice = null;

    public UserMessage(String user, String content, Date timestamp) {
        this.user = user;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Device getTargetDevice() {
        return targetDevice;
    }

    public void setTargetDevice(Device targetDevice){
        if(this.targetDevice == null){
            this.targetDevice = targetDevice;
        }
    }
}
