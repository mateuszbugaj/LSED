package StreamingService;

import Devices.ExternalDevice;
import Devices.DeviceChangeSubscriber;
import Interpreter.Interpreter;
import Utils.Publisher;
import Utils.Subscriber;
import State.ChatState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// ChatManager is equivalent to the DeviceManager
public class ChatManager implements Subscriber<UserMessage>, Publisher<UserMessage> {
    private static final Logger logger = LoggerFactory.getLogger(ChatManager.class);
    private final ArrayList<Chat> chats = new ArrayList<>();
    private final ObservableList<UserMessage> chatMessages = FXCollections.observableArrayList();
    private ArrayList<Subscriber<UserMessage>> userMessageSubscribers = new ArrayList<>();

    public void addChat(Chat chat){
        logger.info("Adding chat " + chat.getChatName());
        chats.add(chat);
        chat.addSubscriber(this);
//
//        // create complementary Device Send Command
//        Command command = new Command(device);
//        deviceSendCommand.put(device, command);
    }

    public List<Chat> getChats(){
        return new ArrayList<>(chats);
    }

    public Chat getChat(int id){
        return chats.get(id);
    }

    public ObservableList<UserMessage> getChatMessages() {
        return chatMessages;
    }

    public void sendMessage(UserMessage message){
        logger.debug("Sending message to all added chats: " + message.getContent());
        chats.forEach(chat -> chat.sendMessage(message.getContent()));
        chatMessages.add(message);
    }

    @Override
    public void update(UserMessage userMessage) {
        logger.debug("Chat manager registered " + userMessage.getMessageType() + " : " + userMessage.getContent());

        // todo: something like a factory pattern to produce different subtypes of message like UserMessage, UserCommand, AdminMessage based on the content and the author. Keep it simple and short

        if(Interpreter.isCommand(userMessage)){
            userMessage.setMessageType(MessageType.USER_COMMAND);
            userMessageSubscribers.forEach(i -> i.update(userMessage)); // todo: should be userCommandSubscribers
        }

//        if(userMessage.getMessageType() == MessageType.NONE){
//            userMessage.setMessageType(MessageType.USER_MESSAGE);
//        }
//
////        if(userMessage.getMessageType() == MessageType.USER_MESSAGE || userMessage.getMessageType() == MessageType.ADMIN_MESSAGE){
////            if(currentSelectedDevice != null){
////                // todo: the message could look like this: "/dev1 command parameter" "/microscope command parameter; command parameter" "/sys command parameter"
////                userMessage.setTargetDevice(currentSelectedDevice);
////            }
////        }
//
//        if((userMessage.getMessageType() == MessageType.USER_MESSAGE || userMessage.getMessageType() == MessageType.ADMIN_MESSAGE) && Interpreter.isCommand(userMessage)){
//            userMessage.setMessageType(MessageType.USER_COMMAND);
//            userMessageSubscribers.forEach(i -> i.update(userMessage)); // todo: should be userCommandSubscribers
//        }

        chatMessages.add(userMessage);
    }

    @Override
    public void addSubscriber(Subscriber<UserMessage> subscriber) {
        userMessageSubscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber<UserMessage> subscriber) {
        userMessageSubscribers.remove(subscriber);
    }
}
