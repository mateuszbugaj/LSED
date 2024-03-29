package Devices;

import StreamingService.Message;
import StreamingService.User;
import Utils.LSEDConfig;
import View.Camera;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class ExternalDevice implements Device, ReceivedMessagesPublisher, CurrentStatePublisher {
    private static final Logger logger = LoggerFactory.getLogger(ExternalDevice.class);
    private final String deviceName;
    private final SerialCom serialCom;
    private final ArrayList<ReceivedMessage> receivedMessages = new ArrayList<>();
    private final ArrayList<ReceivedMessagesSubscriber> receivedMessageSubscriber = new ArrayList<>();
    private final ArrayList<CurrentStateSubscriber> currentStateSubscriber = new ArrayList<>();
    private final List<Camera> cameras;
    private Camera selectedCamera;
    private final List<DeviceCommand> deviceCommands; // available device commands
//    private final Stack<String> deviceInstructions = new Stack<>();
    private final Stack<DeviceCommand> deviceCommandsToExecute = new Stack<>(); // device commands in the queue to be executed
    private Boolean waitingForConformation = false;
    private final Integer timeoutTimer = 100; // sec
    private final Thread deviceThread;
    private DeviceState currentState;
    private String confirmation;

    public ExternalDevice(String deviceName, SerialCom serialCom, List<Camera> cameras, List<DeviceCommand> commands, String initialState, String confirmation){
        this.deviceName = deviceName;
        this.serialCom = serialCom;
        this.cameras = cameras;
        this.deviceCommands = commands;
        this.currentState = new DeviceState(initialState);
        this.confirmation = confirmation;

        if(cameras.size() > 0){
            selectedCamera = cameras.get(0);
        } else {
            selectedCamera = new Camera("No cameras", "");
        }

        if(serialCom != null){
            serialCom.addDataListener(new SerialPortDataListenerImpl(this));
        }

        deviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Device thread is running");
                while(true){
                    if(!waitingForConformation && !deviceCommandsToExecute.empty()){
                        DeviceCommand deviceCommand = deviceCommandsToExecute.pop();
                        while(!deviceCommand.getDeviceInstructions().empty()){
                            if(!waitingForConformation){
                                try {
                                    sendMessage(deviceCommand.getDeviceInstructions().pop());
                                    if(serialCom != null && serialCom.getPortName() !=null ){
                                        if(!confirmation.equals("")){
                                            waitingForConformation = true;
                                        }
                                        logger.debug("Device is waiting for instruction confirmation");
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                try {
                                    Thread.sleep(200);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }

                        if(!deviceCommand.getResultingState().isBlank()){
                            currentState = new DeviceState(deviceCommand.getResultingState());
                            currentStateSubscriber.forEach(i -> i.updateCurrentState(currentState));
                        }
                    }
                }
            }
        });

        deviceThread.start();
    }

    // todo: temp, should not be public and only available by the serial port
    public void receiveMessage(ReceivedMessage receivedMessage){
        logger.debug("Received message: " + receivedMessage.getContent());
        receivedMessages.add(receivedMessage); // todo: is this list even used?
        if(waitingForConformation && receivedMessage.getContent().trim().endsWith(confirmation)){
            logger.debug("Device is no longer waiting");
            waitingForConformation = false;
        }

        LSEDConfig.get().getLogRegister().log(receivedMessage);
        receivedMessageSubscriber.forEach(s -> s.addReceivedMessage(receivedMessage));
    }

    @Override
    public String getName(){
        return deviceName;
    }

    public String getPortName(){
        return serialCom.getPortName();
    }

    public void sendMessage(String message) throws IOException {
        logger.debug("Sending message: " + message);
        serialCom.sendMessage(message + '\n' + '\r');
        LSEDConfig.get().getLogRegister().log(new Message(new User("DeviceManager -> " + deviceName), message, new Date()));

        if(serialCom != null || serialCom.getPortName() != null){
            receivedMessageSubscriber.forEach(s -> s.addReceivedMessage(new ReceivedMessage(deviceName ,message, new Date())));
        }
    }

    public ArrayList<ReceivedMessage> getReceivedMessagesList(){
        return receivedMessages;
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public List<DeviceCommand> getCommands() {
        return deviceCommands;
    }

    public void addCommandsToExecute(List<DeviceCommand> deviceCommands){
        for(DeviceCommand command: deviceCommands){
            addCommandToExecute(command);
        }
    }
    @Override
    public void addCommandToExecute(DeviceCommand deviceCommand){
        deviceCommandsToExecute.push(deviceCommand);
    }

    public Stack<DeviceCommand> getDeviceCommandsToExecute() {
        return deviceCommandsToExecute;
    }

    public Camera changeCamera(String cameraName) throws Throwable {
        logger.debug("Changing camera to " + cameraName);
        Camera camera = cameras
                .stream()
                .filter(i -> i.getName().equals(cameraName))
                .findAny()
                .orElseThrow((Supplier<Throwable>) () -> new RuntimeException("No camera with name " + cameraName + " found"));

        selectedCamera = camera;
        return camera;
    }

    public Camera changeCamera(int cameraId) throws Throwable {
        try{
            logger.debug("Changing camera to " + cameraId);
            Camera camera = cameras.get(cameraId);
            selectedCamera = camera;
            return camera;
        } catch (IndexOutOfBoundsException e){
            throw new Throwable("No camera with id " + cameraId + " found");
        }
    }

    public Camera getSelectedCamera() {
        return selectedCamera;
    }

    public String getCurrentState() {
        if(currentState.getState() == null){
            return  "Not a FSM";
        }

        return currentState.getState();
    }

    @Override
    public void addReceivedMessageSubscriber(ReceivedMessagesSubscriber subscriber) {
        receivedMessageSubscriber.add(subscriber);
    }

    @Override
    public void removeReceivedMessageSubscriber(ReceivedMessagesSubscriber subscriber) {
        receivedMessageSubscriber.remove(subscriber);
    }

    @Override
    public void addCurrentStateSubscriber(CurrentStateSubscriber subscriber) {
        currentStateSubscriber.add(subscriber);
    }

    @Override
    public void removeCurrentStateSubscriber(CurrentStateSubscriber subscriber) {
        currentStateSubscriber.remove(subscriber);
    }

    @Override
    public String toString() {
        return "ExternalDevice{" +
                "deviceName='" + deviceName + '\'' +
                ", serialCom=" + serialCom +
                ", cameras=" + cameras +
                ", deviceCommands=" + deviceCommands +
                ", currentState=" + currentState +
                '}';
    }
}
