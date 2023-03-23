package Devices;

import Interpreter.Interpreter;
import State.DeviceMediator;
import StreamingService.*;
import Utils.ReturnMessageException;
import View.Command;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

public class DeviceManager implements Device, MessageSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(DeviceManager.class);
    private final List<ExternalDevice> devices = new ArrayList<>();
    private final Map<ExternalDevice, DeviceMediator> deviceStates = new HashMap<>();
    private final Map<ExternalDevice, Command> deviceSendCommand = new HashMap<>();
    private final String systemName = "sys";
    private final List<DeviceCommand> systemCommands = new ArrayList<>();

    /*
    todo: change of the selected device index should be a major signal and more thought should be put into securing
    todo: the low coupling as well as good availability of this command across components.Think what design pattern would allow it.
    todo: Whole View GUI is going to relly on this and also few aspects from Options GUI.
     */
    public IntegerProperty selectedDeviceIndex = new SimpleIntegerProperty(0);
    private ArrayList<DeviceChangeSubscriber> deviceChangeSubscribers = new ArrayList<>();
    private final ChatManagerMediator mediator;
    private final UserManager userManager;

    public DeviceManager(ChatManagerMediator mediator, UserManager userManager) {
        this.mediator = mediator;
        this.userManager = userManager;
        selectedDeviceIndex.addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() == -1) return;
            changeSelectedDevice((Integer) newValue);
        });

        DeviceCommandParam changeCameraByNameCommandParam1 = new DeviceCommandParam(
                "Camera Name",
                DeviceCommandParamType.String,
                List.of(),
                0,
                Integer.MAX_VALUE,
                false,
                "false");
        DeviceCommand changeCameraCommandByName = new DeviceCommand(
                "Change camera",
                "Change main camera to one of available for selected device by providing camera name",
                "cc",
                "camera",
                List.of(changeCameraByNameCommandParam1),
                List.of(),
                List.of(),
                "");
        systemCommands.add(changeCameraCommandByName);

        DeviceCommandParam changeCameraByIdCommandParam1 = new DeviceCommandParam(
                "Camera ID",
                DeviceCommandParamType.Integer,
                List.of(),
                0,
                Integer.MAX_VALUE,
                false,
                "false");
        DeviceCommand changeCameraCommandById = new DeviceCommand(
                "Change camera",
                "Change main camera to one of available for selected device  by providing camera ID",
                "cc",
                "camera",
                List.of(changeCameraByIdCommandParam1),
                List.of(),
                List.of(),
                "");
        systemCommands.add(changeCameraCommandById);

        DeviceCommandParam changeDeviceByIdCommandParam1 = new DeviceCommandParam(
                "Device id",
                DeviceCommandParamType.Integer,
                List.of(),
                0,
                Integer.MAX_VALUE,
                false,
                "false");
        DeviceCommand changeDeviceCommandById = new DeviceCommand(
                "Change device",
                "Change selected device by providing ID",
                "cd",
                "device",
                List.of(changeDeviceByIdCommandParam1),
                List.of(),
                List.of(),
                "");
        systemCommands.add(changeDeviceCommandById);

        DeviceCommandParam changeDeviceByNameCommandParam1 = new DeviceCommandParam(
                "Device name",
                DeviceCommandParamType.String,
                List.of(),
                0,
                Integer.MAX_VALUE,
                false,
                "false");
        DeviceCommand changeDeviceCommandByName = new DeviceCommand(
                "Change device",
                "Change selected device by providing name",
                "cd",
                "device",
                List.of(changeDeviceByNameCommandParam1),
                List.of(),
                List.of(),
                "");
        systemCommands.add(changeDeviceCommandByName);
    }

    public void addDevice(ExternalDevice device){
        logger.info("Adding external device " + device);
        devices.add(device);

        // create complementary DeviceState
        DeviceMediator deviceMediator = new DeviceMediator();
        device.addReceivedMessageSubscriber(deviceMediator);
        device.addCurrentStateSubscriber(deviceMediator);
        deviceStates.put(device, deviceMediator); // todo: Maybe DeviceState should hold it's corresponding device like that? 'new DeviceState(Device d);'

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

    public DeviceMediator getDeviceState(ExternalDevice device){
        return deviceStates.get(device);
    }

    public DeviceMediator getDeviceState(Integer id){
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

    public void changeSelectedDevice(String name){
        // todo: device should hold it's ID in field
        for(int i = 0; i < devices.size(); i++){
            if(devices.get(i).getName().equals(name)){
                changeSelectedDevice(i);
                return;
            }
        }
    }

    public void changeSelectedDevice(Integer id){
        logger.debug("Change device to device nr: " + id);
        selectedDeviceIndex.set(id);
        deviceChangeSubscribers.forEach(i -> i.deviceUpdate(devices.get(id)));
    }

    @Override
    public void handleMessage(Message message) throws ReturnMessageException {
        logger.debug("Received device user message: " + message);
        if(!message.getUser().hasAdminPrivileges() && userManager.getActiveUser().get() != null && !message.getUser().getName().equals(userManager.getActiveUser().get().getName())){
            return;
        }

        if(message.getMessageType().equals(MessageType.SYSTEM_COMMAND) || message.getMessageType().equals(MessageType.DEVICE_COMMAND)){
            try{
                List<DeviceCommand> deviceCommandsToExecute = Interpreter.interpret(message);
                logger.debug("Received following deviceInstructions form Interpreter: " + deviceCommandsToExecute.stream().map(DeviceCommand::getDeviceInstructions).toList());
                for(DeviceCommand command:deviceCommandsToExecute){
                    message.getTargetDevice().addCommandToExecute(command);
                }

            } catch (ReturnMessageException infoException){
                logger.error(infoException.toString());

                 /* Sometimes the error gets send to the MainWindow before the actual message.
                    This short delay allows the command to be displayed before it. */
                 try {
                     Thread.sleep(10);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
                throw infoException;
            }
        }
    }

    @Override
    public void annotateMessage(Message message) {
        // Check if message is a device command or a system command
        if(message.getMessageType().equals(MessageType.COMMAND)){
            String messageTargetDevice = message.getContent().split(" ")[0].replaceFirst("!", "");

            if(messageTargetDevice.equals(systemName)){
                message.setTargetDevice(this);
                message.setType(MessageType.SYSTEM_COMMAND);
                return;
            }

            Optional<ExternalDevice> externalDevice;

            try{
                int deviceIndex = Integer.parseInt(messageTargetDevice);
                externalDevice = Optional.ofNullable(devices.get(deviceIndex - 1));
            } catch (NumberFormatException e){
                externalDevice = devices.stream()
                        .filter(i -> i.getName().equals(messageTargetDevice))
                        .findFirst();

            } catch (IndexOutOfBoundsException e){
                return;
            }

            if(externalDevice.isPresent()){
                message.setTargetDevice(externalDevice.get());
                message.setType(MessageType.DEVICE_COMMAND);
            }
        }
    }

    @Override
    public String getName() {
        return "sys";
    }

    @Override
    public void addCommandToExecute(DeviceCommand command) throws ReturnMessageException{
        String instruction = command.getDeviceInstructions().pop();
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
                throw new ReturnMessageException(e.getMessage());
            }
        }

        if(instruction.split(" ")[0].equals("device")){
            try{
                String deviceName = instruction.split(" ")[1];
                if(Pattern.compile("-?\\d+(\\.\\d+)?").matcher(deviceName).matches()){
                    changeSelectedDevice(Integer.parseInt(deviceName) - 1);
                } else {
                    changeSelectedDevice(deviceName);
                }

                /* this just sends device change signal again to refresh GUI */
                int temp = selectedDeviceIndex.get();
                selectedDeviceIndex.set(-1);
                selectedDeviceIndex.set(temp);
            } catch (Throwable e) {
                logger.error(e.toString());
                throw new ReturnMessageException(e.getMessage());
            }
        }
    }

    @Override
    public List<DeviceCommand> getCommands() {
        return systemCommands;
    }

    @Override
    public String getCurrentState() {
        return ""; // Not a state machine
    }
}
