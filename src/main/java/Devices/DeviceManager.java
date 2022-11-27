package Devices;

import Interpreter.Interpreter;
import State.DeviceState;
import StreamingService.MessageType;
import StreamingService.UserMessage;
import Utils.Publisher;
import Utils.Subscriber;
import View.Command;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class DeviceManager implements Subscriber<UserMessage>, Publisher<UserMessage>, Device { // todo: It should not subscribe to the UserMessage but to UserCommand subtype
    private static final Logger logger = LoggerFactory.getLogger(DeviceManager.class);
    private final List<ExternalDevice> devices = new ArrayList<>();
    private final Map<ExternalDevice, DeviceState> deviceStates = new HashMap<>();
    private final Map<ExternalDevice, Command> deviceSendCommand = new HashMap<>();
    private final ArrayList<Subscriber<UserMessage>> userMessageSubscribers = new ArrayList<>();

    private final String systemName = "sys";
    private final List<DeviceCommand> systemCommands = new ArrayList<>();

    /*
    todo: change of the selected device index should be a major signal and more thought should be put into securing
    todo: the low coupling as well as good availability of this command across components.Think what design pattern would allow it.
    todo: Whole View GUI is going to relly on this and also few aspects from Options GUI.
     */
    public IntegerProperty selectedDeviceIndex = new SimpleIntegerProperty(0);
    private ArrayList<DeviceChangeSubscriber> deviceChangeSubscribers = new ArrayList<>();

    public DeviceManager() {
        selectedDeviceIndex.addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() == -1) return;
            changeSelectedDevice((Integer) newValue);
        });

        DeviceCommandParam changeCameraByNameCommandParam1 = new DeviceCommandParam(
                "Camera Name",
                DeviceCommandParamType.String,
                List.of(),
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                false,
                null);
        DeviceCommand changeCameraCommandByName = new DeviceCommand(
                "Change camera",
                "Change main camera to one of available for selected device",
                "cc",
                "camera",
                List.of(changeCameraByNameCommandParam1),
                List.of());
        systemCommands.add(changeCameraCommandByName);

        DeviceCommandParam changeCameraByIdCommandParam1 = new DeviceCommandParam(
                "Camera Name",
                DeviceCommandParamType.String,
                List.of(),
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                false,
                null);
        DeviceCommand changeCameraCommandById = new DeviceCommand(
                "Change camera",
                "Change main camera to one of available for selected device",
                "cc",
                "camera",
                List.of(changeCameraByIdCommandParam1),
                List.of());
        systemCommands.add(changeCameraCommandById);
    }

    public void addDevice(ExternalDevice device){
        logger.info("Adding external device " + device);
        devices.add(device);

        // create complementary DeviceState
        DeviceState deviceState = new DeviceState(); // todo: Maybe DeviceState should hold it's corresponding device like that? 'new DeviceState(Device d);'
        device.addSubscriber(deviceState);
        deviceStates.put(device, deviceState);

        // create complementary Device Send Command
        Command command = new Command(device);
        deviceSendCommand.put(device, command);
    }

    public List<ExternalDevice> getDevices(){
        return new ArrayList<>(devices);
    }

    public ExternalDevice getDevice(int id){
        return devices.get(id);
    }

    public DeviceState getDeviceState(ExternalDevice device){
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

    public Command getDeviceSendCommand(ExternalDevice device){
        return deviceSendCommand.get(device);
    }

    public void addDeviceChangeSubscriber(DeviceChangeSubscriber subscriber){
        deviceChangeSubscribers.add(subscriber);
    }

    public void changeSelectedDevice(Integer id){
        logger.debug("Change device to device nr: " + id);
        deviceChangeSubscribers.forEach(i -> i.deviceUpdate(devices.get(id)));
    }

    // todo: refactor this method
    @Override
    public void update(UserMessage userMessage) {
        logger.debug("Received device user message: " + userMessage);

        // Check if message is addressed to the device (by name or by index number)
        if(userMessage.getContent().startsWith("/")){
            String messageTargetDevice = userMessage.getContent().split(" ")[0].replaceFirst("/", "");
            if(messageTargetDevice.equals("sys")){
                logger.debug("Message addresses the system");
                userMessage.setTargetDevice(this);

                try{
                    ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);
                    logger.debug("Received following deviceInstructions form Interpreter: " + deviceInstructions);

                    for(String deviceInstruction : deviceInstructions){
                        this.addInstruction(deviceInstruction);
                    }

                } catch (Throwable e){
                    userMessageSubscribers.forEach(i -> i.update(new UserMessage("Interpreter", e.getMessage(), new Date()).setMessageType(MessageType.INTERPRETER_MESSAGE)));
                }
            } else {
                Optional<ExternalDevice> externalDevice;

                try{
                    int deviceIndex = Integer.parseInt(messageTargetDevice);
                    externalDevice = Optional.ofNullable(devices.get(deviceIndex - 1));
                } catch (NumberFormatException e){
                    externalDevice = devices.stream()
                            .filter(i -> i.getName().equals(messageTargetDevice))
                            .findFirst();
                } catch (IndexOutOfBoundsException e){
                    userMessageSubscribers.forEach(i -> i.update(new UserMessage("Interpreter", "Device with ID " + messageTargetDevice + " not found", new Date()).setMessageType(MessageType.INTERPRETER_MESSAGE)));
                    return;
                }

                if(externalDevice.isPresent()){
                    userMessage.setTargetDevice(externalDevice.get());

                    try{
                        ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);
                        logger.debug("Received following deviceInstructions form Interpreter: " + deviceInstructions);

                        for(String deviceInstruction : deviceInstructions){
                            externalDevice.get().addInstruction(deviceInstruction);
                        }

                    } catch (Throwable e){
                        logger.error(e.toString());
                        userMessageSubscribers.forEach(i -> i.update(new UserMessage("Interpreter", e.toString(), new Date()).setMessageType(MessageType.INTERPRETER_MESSAGE)));
                    }
                } else {
                    userMessageSubscribers.forEach(i -> i.update(new UserMessage("Interpreter", "Device " + messageTargetDevice + " not found", new Date()).setMessageType(MessageType.INTERPRETER_MESSAGE)));
                }
            }
        }
    }

    @Override
    public void addSubscriber(Subscriber<UserMessage> subscriber) {
        userMessageSubscribers.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber<UserMessage> subscriber) {
        userMessageSubscribers.remove(subscriber);
    }

    @Override
    public void receiveMessage(ReceivedMessage receivedMessage) {

    }

    @Override
    public String getName() {
        return "sys";
    }

    @Override
    public void addInstruction(String instruction) {
        logger.debug("System received instruction: " + instruction);
        if(instruction.split(" ")[0].equals("camera")){
            try{
                String cameraName = instruction.split(" ")[1];
                if(Pattern.compile("-?\\d+(\\.\\d+)?").matcher(cameraName).matches()){
                    devices.get(selectedDeviceIndex.get()).changeCamera(Integer.parseInt(cameraName) - 1);
                } else {
                    devices.get(selectedDeviceIndex.get()).changeCamera(cameraName);
                }

                /* this just sends device change signal again to refresh GUI */
                int temp = selectedDeviceIndex.get();
                selectedDeviceIndex.set(-1);
                selectedDeviceIndex.set(temp);
            } catch (Throwable e) {
                logger.error(e.toString());
                userMessageSubscribers.forEach(i -> i.update(new UserMessage("Interpreter", e.getMessage(), new Date()).setMessageType(MessageType.INTERPRETER_MESSAGE)));
            }
        }
    }

    @Override
    public List<DeviceCommand> getCommands() {
        return systemCommands;
    }
}
