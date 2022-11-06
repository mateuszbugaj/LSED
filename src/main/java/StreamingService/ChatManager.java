package StreamingService;

import Device.Device;
import Device.DeviceChangeSubscriber;
import Interpreter.Interpreter;
import Utils.Publisher;
import Utils.Subscriber;
import State.ChatState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// ChatManager is equivalent to the DeviceManager
public class ChatManager implements Subscriber<UserMessage>, DeviceChangeSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(ChatManager.class);
    private final ArrayList<Chat> chats = new ArrayList<>();
    private final Map<Chat, ChatState> chatStates = new HashMap<>(); // todo: this might not be used or needed
//    private final ArrayList<ReceivedMessage> chatMessages = new ArrayList<>();
    private final ObservableList<UserMessage> chatMessages = FXCollections.observableArrayList();
    private Device currentSelectedDevice;

    public void addChat(Chat chat){
        logger.info("Adding chat " + chat.getChatName());
        chats.add(chat);

        ChatState chatState = new ChatState();
//        chat.addSubscriber(chatState);
        chat.addSubscriber(this);
        chatStates.put(chat, chatState);
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

        if(userMessage.getMessageType() == MessageType.NONE){
            userMessage.setMessageType(MessageType.USER_MESSAGE);
        }

        if(userMessage.getMessageType() == MessageType.USER_MESSAGE || userMessage.getMessageType() == MessageType.ADMIN_MESSAGE){
            if(currentSelectedDevice != null){
                userMessage.setTargetDevice(currentSelectedDevice);
            }
        }

        if((userMessage.getMessageType() == MessageType.USER_MESSAGE || userMessage.getMessageType() == MessageType.ADMIN_MESSAGE) && Interpreter.isCommand(userMessage)){
            userMessage.setMessageType(MessageType.USER_COMMAND);
        }

        chatMessages.add(userMessage);

        try{
            // todo: think how to act on commands meant for the system (to change device for example)
            ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);
            logger.debug("Received following deviceInstructions form Interpreter: " + deviceInstructions);
            deviceInstructions.forEach(i -> currentSelectedDevice.addDeviceInstruction(i));
        } catch (Throwable e){
            chatMessages.add(new UserMessage("Interpreter", e.getMessage(), new Date()).setMessageType(MessageType.INTERPRETER_MESSAGE));
        }
    }

    @Override
    public void update(Device newSelectedDevice) {
        logger.debug("Current selected device updated: " + newSelectedDevice.getDeviceName());
        currentSelectedDevice = newSelectedDevice;
    }
}
