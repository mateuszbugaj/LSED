package StreamingService;

public interface ChatManagerMediator {
    void handleNewMessage(Message newMessage);
    void handleNewMessage(String message, String userName);
}
