package Device;

import State.DeviceState;
import View.Command;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// here will be service discovery for devices which means it will read from file where are device yamls and create devices based on this (factory pattern?)
public class DeviceManager {
    private static final Logger logger = LoggerFactory.getLogger(DeviceManager.class);
    private final ArrayList<Device> devices = new ArrayList<>();
    private final Map<Device, DeviceState> deviceStates = new HashMap<>();
    private final Map<Device, Command> deviceSendCommand = new HashMap<>();

    /*
    todo: change of the selected device index should be a major signal and more thought should be put into securing
    todo: the low coupling as well as good availability of this command across components.Think what design pattern would allow it.
    todo: Whole View GUI is going to relly on this and also few aspects from Options GUI.
     */
    public IntegerProperty selectedDeviceIndex = new SimpleIntegerProperty(0);
    private ArrayList<DeviceChangeSubscriber> deviceChangeSubscribers = new ArrayList<>();

    public DeviceManager() {
        selectedDeviceIndex.addListener((observable, oldValue, newValue) -> {
            changeSelectedDevice((Integer) newValue);
        });
    }

    public void addDevice(Device device){
        logger.info("Adding device " + device.getDeviceName());
        devices.add(device);

        // create complementary DeviceState
        DeviceState deviceState = new DeviceState(); // todo: Maybe DeviceState should hold it's corresponding device like that? 'new DeviceState(Device d);'
        device.addSubscriber(deviceState);
        deviceStates.put(device, deviceState);

        // create complementary Device Send Command
        Command command = new Command(device);
        deviceSendCommand.put(device, command);
    }

    public List<Device> getDevices(){
        return new ArrayList<>(devices);
    }

    public Device getDevice(int id){
        return devices.get(id);
    }

    public DeviceState getDeviceState(Device device){
        return deviceStates.get(device);
    }

    public DeviceState getDeviceState(Integer id){
        if(id < devices.size()){
            return getDeviceState(devices.get(id));
        }

        logger.error("Device ID is out of bounds");
        return null;
    }

    public Command getDeviceSendCommand(Integer id){
        return deviceSendCommand.get(devices.get(id));
    }

    public Command getDeviceSendCommand(Device device){
        return deviceSendCommand.get(device);
    }

    public void addDeviceChangeSubscriber(DeviceChangeSubscriber subscriber){
        deviceChangeSubscribers.add(subscriber);
    }

    public void changeSelectedDevice(Integer id){
        logger.debug("Change device to device nr: " + id);
        deviceChangeSubscribers.forEach(i -> i.update(devices.get(id)));
    }
}
