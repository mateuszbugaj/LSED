package Devices;

import java.util.List;

public interface Device {
    void receiveMessage(ReceivedMessage receivedMessage);
    String getName();
    void addCommandToExecute(DeviceCommand command);
    List<DeviceCommand> getCommands();
    String getCurrentState();
}
