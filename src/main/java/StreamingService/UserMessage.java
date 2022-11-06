package StreamingService;

import Device.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class UserMessage {
    private static final Logger logger = LoggerFactory.getLogger(UserMessage.class);
    private final String user; // todo: this will be it's own class with properties and privileges read from saved file in DB
    private final String content;
    private final Date timestamp;
    private Device targetDevice = null;
    private MessageType messageType = MessageType.NONE;

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
            logger.debug("Setting targetDevice: " + targetDevice.getDeviceName());
            this.targetDevice = targetDevice;
        }
    }

    public UserMessage setMessageType(MessageType messageType){
        this.messageType = messageType;

        return this;
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
