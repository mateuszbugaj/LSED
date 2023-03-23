package Utils;

import StreamingService.MessageType;

public class ReturnMessageException extends Exception {
    private final MessageType messageType;
    public ReturnMessageException(String content) {
        super(content);
        messageType = MessageType.ERROR;
    }

    public ReturnMessageException(String content, MessageType type) {
        super(content);
        if(type == MessageType.INFO || type == MessageType.ERROR){
            messageType = type;
        } else {
            messageType = MessageType.ERROR;
        }
    }

    public MessageType getMessageType() {
        return messageType;
    }
}
