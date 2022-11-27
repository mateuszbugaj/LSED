package Devices;

import java.util.List;

public interface Device {
    void receiveMessage(ReceivedMessage receivedMessage);
    String getName();
    void addInstruction(String instruction);
    List<DeviceCommand> getCommands();
}
