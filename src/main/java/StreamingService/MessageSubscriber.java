package StreamingService;

import Utils.ReturnMessageException;

public interface MessageSubscriber {
    void annotateMessage(Message message);
    void handleMessage(Message message) throws ReturnMessageException;
}