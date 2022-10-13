package State;

import Device.ReceivedMessage;
import Utils.Subscriber;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// This class is in the middle of View and Model (controller?)
// This class could implement multiple Subscriber interfaces for many parameters
//
// This could also be a facade pattern? To encapsulate existing class and add library-specific functionality (fx properties)
public class DeviceState implements Subscriber<ReceivedMessage> {
    //    BooleanProperty selected;
    public ObservableList<ReceivedMessage> receivedMessages;

    public DeviceState() {
        receivedMessages = FXCollections.observableArrayList();
    }

    @Override
    public void update(ReceivedMessage newMessage) {
        Platform.runLater(() -> receivedMessages.add(newMessage));
    }
}
