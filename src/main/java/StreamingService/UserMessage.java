package StreamingService;

import Devices.Device;
import Devices.ExternalDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserMessage {
    private static final Logger logger = LoggerFactory.getLogger(UserMessage.class);
    private static Integer ID_COUNT = 0;
    private final Integer id;
    private final String user; // todo: this will be it's own class with properties and privileges read from saved file in DB
    private final String content;
    private final Date timestamp;
    private MessageType messageType = MessageType.NONE; // Assigned by ChatManager
    private Device targetDevice = null; // Assigned by DeviceManager if the messageType is USER_COMMAND

    public UserMessage(String user, String content, Date timestamp) {
        this.id = ID_COUNT++;
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
            logger.debug("ID " + id + " - Setting targetDevice: " + targetDevice.getName());
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
