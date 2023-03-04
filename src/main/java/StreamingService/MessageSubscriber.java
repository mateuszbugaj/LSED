package StreamingService;

public interface MessageSubscriber {
    void annotateMessage(Message message);
    void handleMessage(Message message);
}