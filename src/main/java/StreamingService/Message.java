package StreamingService;

import Devices.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {
    private static final Logger logger = LoggerFactory.getLogger(Message.class);
    private static Integer ID_COUNT = 0;
    private final Integer id;
//    private final String user; // todo: this will be it's own class with properties and privileges read from saved file in DB
    private final User user;
    private final String content;
    private final Date timestamp;
    private MessageType messageType = MessageType.NONE;
    private MessageOwnership messageOwnership = MessageOwnership.NONE;
    private Device targetDevice = null; // Assigned by DeviceManager if the messageType is USER_COMMAND

    public Message(User user, String content) {
        this.id = ID_COUNT++;
        this.user = user;
        this.content = content;
        this.timestamp = new Date();
    }

    public Message(User user, String content, Date timestamp) {
        this.id = ID_COUNT++;
        this.user = user;
        this.content = content;
        this.timestamp = timestamp;
    }

    public User getUser() {
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

    public Message setTargetDevice(Device targetDevice){
        if(this.targetDevice == null){
            logger.debug("ID " + id + " - Setting targetDevice: " + targetDevice.getName());
            this.targetDevice = targetDevice;
        }

        return this;
    }

    public Message setType(MessageType messageType){
        this.messageType = messageType;

        return this;
    }

    public Message setOwnership(MessageOwnership messageOwnership){
        if(this.messageOwnership == MessageOwnership.NONE){
            this.messageOwnership = messageOwnership;
        }

        return this;
    }

    public MessageOwnership getMessageOwnership() {
        return messageOwnership;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String toString() {
        return "UserMessage{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + new SimpleDateFormat("HH:mm:ss").format(timestamp) +
                '}';
    }
}
