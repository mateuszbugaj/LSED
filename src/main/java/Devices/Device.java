package Devices;

import java.util.List;

public interface Device {
    String getName();
    void addCommandToExecute(DeviceCommand command);
    List<DeviceCommand> getCommands();
    String getCurrentState();
}
