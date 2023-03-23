package StreamingService;

import Interpreter.Interpreter;
import Utils.ReturnMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ChatManager implements ChatManagerMediator{
    private static final Logger logger = LoggerFactory.getLogger(ChatManager.class);
    private final ArrayList<ChatService> chats = new ArrayList<>();
    private final ArrayList<Message> chatMessages = new ArrayList<>();
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

    public ArrayList<Message> getChatMessages() {
        return chatMessages;
    }

    @Override
    public void handleNewMessage(String messageContent, String userName) {
        logger.debug("Chat manager registered message : " + messageContent + " - " + userName);

        User user = userManager.getUser(userName);
        Message newMessage = new Message(user, messageContent, new Date());
        handleNewMessage(newMessage);
    }

    @Override
    public void handleNewMessage(Message newMessage) {
        MessageOwnership messageOwnership;
        String messageOwner = newMessage.getUser().getName();
        if(userManager.getUser(messageOwner).hasAdminPrivileges()) {
            messageOwnership = MessageOwnership.ADMIN;
        } else if(messageOwner.equals("Interpreter")){
            messageOwnership = MessageOwnership.INTERPRETER;
        } else {
            messageOwnership = MessageOwnership.USER;
        }

        newMessage.setOwnership(messageOwnership);

        if(Interpreter.isCommand(newMessage)){
            newMessage.setType(MessageType.COMMAND);
        } else if(newMessage.getMessageType() == MessageType.NONE) {
            newMessage.setType(MessageType.MESSAGE);
        }

        chatMessages.add(newMessage);
        messageSubscribers.forEach(sub -> sub.annotateMessage(newMessage));

        messageSubscribers.forEach(sub -> {
            try {
                sub.handleMessage(newMessage);
            } catch (ReturnMessageException infoException) {
                logger.error(infoException.getMessage());
                Message infoMessage = new Message(userManager.getUser("Interpreter"), infoException.getMessage()).setType(infoException.getMessageType());
                handleNewMessage(infoMessage);
            }
        });

        if(newMessage.getMessageType() == MessageType.COMMAND){
            // If command message is not DEVICE_COMMAND, SYSTEM_COMMAND nor CONTROL_COMMAND then it is invalid
            Message infoMessage = new Message(userManager.getUser("Interpreter"), "Device not found").setType(MessageType.ERROR);
            handleNewMessage(infoMessage);
        }
    }

    public void addMessageSubscriber(MessageSubscriber subscriber){
        messageSubscribers.add(subscriber);
    }
}
