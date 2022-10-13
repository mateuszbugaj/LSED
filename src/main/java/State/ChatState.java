package State;

import Utils.Subscriber;
import StreamingService.UserMessage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatState implements Subscriber<UserMessage> {
    public ObservableList<UserMessage> receivedMessages;

    public ChatState() {
        receivedMessages = FXCollections.observableArrayList();
    }

    @Override
    public void update(UserMessage newMessage) {
        Platform.runLater(() -> receivedMessages.add(newMessage));
    }
}
