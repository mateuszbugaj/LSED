package State;

import Utils.Subscriber;
import StreamingService.Message;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatState implements Subscriber<Message> {
    public ObservableList<Message> receivedMessages;

    public ChatState() {
        receivedMessages = FXCollections.observableArrayList();
    }

    @Override
    public void update(Message newMessage) {
        Platform.runLater(() -> receivedMessages.add(newMessage));
    }
}
