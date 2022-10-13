package StreamingService;

import Utils.Subscriber;
import State.ChatState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;

// ChatManager is equivalent to the DeviceManager
public class ChatManager implements Subscriber<UserMessage> {
    private final ArrayList<Chat> chats = new ArrayList<>();
    private final Map<Chat, ChatState> chatStates = new HashMap<>();
//    private final ArrayList<ReceivedMessage> chatMessages = new ArrayList<>();
    private final ObservableList<UserMessage> chatMessages = FXCollections.observableArrayList();

    public void addChat(Chat chat){
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
        chats.forEach(chat -> chat.sendMessage(message.getContent()));
        chatMessages.add(message);
    }

    @Override
    public void update(UserMessage userMessage) {
        chatMessages.add(userMessage);
    }
}
