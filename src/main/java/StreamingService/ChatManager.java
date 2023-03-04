package StreamingService;

import Interpreter.Interpreter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// ChatManager is equivalent to the DeviceManager
public class ChatManager implements ChatManagerMediator{
    private static final Logger logger = LoggerFactory.getLogger(ChatManager.class);
    private final ArrayList<ChatService> chats = new ArrayList<>();
    private final ObservableList<Message> chatMessages = FXCollections.observableArrayList();
    private final UserManager userManager;
    private final List<MessageSubscriber> messageSubscribers;

    public ChatManager(UserManager userManager) {
        this.userManager = userManager;
        messageSubscribers = new ArrayList<>();
    }

    public void addChat(ChatService chat){
        logger.info("Adding chat " + chat.getName());
        chats.add(chat);
    }

    public List<ChatService> getChats(){
        return new ArrayList<>(chats);
    }

    public ObservableList<Message> getChatMessages() {
        return chatMessages;
    }

    @Override
    public void handleNewMessage(String messageContent, String userName) {
        logger.debug("Chat manager registered message : " + messageContent + " - " + userName);

        User user = userManager.getUser(userName);
        Message message = new Message(user, messageContent, new Date());

        MessageOwnership messageOwnership;
        switch (userName.toLowerCase(Locale.ROOT)){
            case "admin":
                messageOwnership = MessageOwnership.ADMIN;
                break;
            case "interpreter":
                messageOwnership = MessageOwnership.INTERPRETER;
                break;
            default:
                messageOwnership = MessageOwnership.USER;
        }

        message.setOwnership(messageOwnership);

        if(Interpreter.isCommand(message)){
            message.setType(MessageType.COMMAND);
        } else {
            message.setType(MessageType.MESSAGE);
        }

        chatMessages.add(message);
        messageSubscribers.forEach(sub -> sub.annotateMessage(message));
        messageSubscribers.forEach(sub -> sub.handleMessage(message));
    }

    public void addMessageSubscriber(MessageSubscriber subscriber){
        messageSubscribers.add(subscriber);
    }
}
