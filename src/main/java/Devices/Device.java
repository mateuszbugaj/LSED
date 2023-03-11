package Devices;

import Utils.ReturnMessageException;

import java.util.List;

public interface Device {
    String getName();
    void addCommandToExecute(DeviceCommand command) throws ReturnMessageException;
    List<DeviceCommand> getCommands();
    String getCurrentState();
}
