package State;

import Devices.CurrentStateSubscriber;
import Devices.DeviceState;
import Devices.ReceivedMessage;
import Devices.ReceivedMessagesSubscriber;
import Utils.Subscriber;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

// This class is in the middle of View and Model (controller?)
// This class could implement multiple Subscriber interfaces for many parameters
//
// This could also be a facade pattern? To encapsulate existing class and add library-specific functionality (fx properties)
public class DeviceMediator implements ReceivedMessagesSubscriber, CurrentStateSubscriber {
    //    BooleanProperty selected;
    public ObservableList<ReceivedMessage> receivedMessages;
    public SimpleObjectProperty<DeviceState> currentState;

    public DeviceMediator() {
        receivedMessages = FXCollections.observableArrayList();
        currentState = new SimpleObjectProperty<>();
    }

    @Override
    public void addReceivedMessage(ReceivedMessage receivedMessage) {
        Platform.runLater(() -> receivedMessages.add(receivedMessage));
    }

    @Override
    public void updateCurrentState(DeviceState state) {
        Platform.runLater(() -> currentState.set(state));
    }
}
