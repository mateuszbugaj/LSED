package Devices;

import Interpreter.Interpreter;
import Utils.Subscriber;
import View.Camera;
import Utils.Publisher;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExternalDevice implements Device, Publisher {
    private static final Logger logger = LoggerFactory.getLogger(ExternalDevice.class);
    private final String deviceName;
    private final SerialCom serialCom;
    private final ArrayList<ReceivedMessage> receivedMessages = new ArrayList<>();
    private final ArrayList<Subscriber<ReceivedMessage>> receivedMessageSubscriber = new ArrayList<>();
    private final List<Camera> cameras;
    private Camera selectedCamera;
    private final List<DeviceCommand> deviceCommands;
    private final Stack<String> deviceInstructions = new Stack<>();
    private Boolean waitingForConformation = false;
    private final Integer timeoutTimer = 100; // sec
    private final Thread deviceThread;

    public ExternalDevice(String deviceName, SerialCom serialCom, List<Camera> cameras, List<DeviceCommand> commands){
        this.deviceName = deviceName;
        this.serialCom = serialCom;
        this.cameras = cameras;
        this.deviceCommands = commands;

        if(cameras.size() > 0){
            selectedCamera = cameras.get(0);
        } else {
            selectedCamera = new Camera("No cameras", "");
        }

        serialCom.addDataListener(new SerialPortDataListenerImpl(this));

        deviceThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Device thread is running");
                while(true){
                    if(!waitingForConformation && !deviceInstructions.empty()){
                        try {
                            sendMessage(deviceInstructions.pop());
                            waitingForConformation = true;
                            logger.debug("Device is waiting for instruction confirmation");
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    try {

                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });

        deviceThread.start();
    }

    // todo: temp, should not be public
    @Override
    public void receiveMessage(ReceivedMessage receivedMessage){
        logger.debug("Received message: " + receivedMessage.getMessage());
        receivedMessages.add(receivedMessage); // todo: is this list even used?
        if(waitingForConformation && receivedMessage.getMessage().compareTo("done") == 0){
            logger.debug("Device is no longer waiting");
            waitingForConformation = false;
        }

        receivedMessageSubscriber.forEach(s -> s.update(receivedMessage));
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

    @Override
    public void addInstruction(String instruction){
        deviceInstructions.push(instruction);
    }

    public List<String> getDeviceInstructions() {
        return new ArrayList<>(deviceInstructions);
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

    @Override
    public void addSubscriber(Subscriber subscriber) {
        receivedMessageSubscriber.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        receivedMessageSubscriber.remove(subscriber);
    }

    @Override
    public String toString() {
        return "ExternalDevice{" +
                "deviceName='" + deviceName + '\'' +
                ", serialCom=" + serialCom +
                ", cameras=" + cameras +
                ", deviceCommands=" + deviceCommands +
                '}';
    }
}
