package Device;

import Interpreter.Interpreter;
import Utils.Subscriber;
import View.Camera;
import Utils.Publisher;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortTimeoutException;
import com.github.sarxos.webcam.Webcam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Supplier;

public class Device implements Publisher {
    private static final Logger logger = LoggerFactory.getLogger(Device.class);
    private final String deviceName;
    private final String portName;
    private final SerialCom serialCom;
    private final String configFile; // todo: Path type?
    private final ArrayList<ReceivedMessage> receivedMessages = new ArrayList<>();
    private final ArrayList<Subscriber<ReceivedMessage>> receivedMessageSubscriber = new ArrayList<>();
    private final ArrayList<Camera> cameras = new ArrayList<>();
    private final ArrayList<DeviceCommand> deviceCommands = new ArrayList<>();
    private final Stack<String> deviceInstructions = new Stack<>();
    private Boolean waitingForConformation = false;
    private Integer timeoutTimer = 100; // sec
    private Thread deviceThread;

    public Device(String configFile) throws Throwable {
        this.configFile = configFile;
        logger.info("Configuring device from file: " + configFile);

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(new FileReader(configFile));

        deviceName = (String) data.get("name");
        logger.debug("Device name: " + deviceName);

        portName = data.get("portName") == null ? null : (String) data.get("portName");
        logger.debug("Port name: " + portName);

        Integer baudRate = (Integer) (data.get("baudRate") == null ? DeviceConfig.DEFAULT_BAUD_RATE : data.get("baudRate"));
        logger.debug("Baud rate: " + baudRate);

        ArrayList<String> cameraNames = data.get("cameras") == null ? new ArrayList<>() : (ArrayList<String>) data.get("cameras");
        logger.debug("Device cameras: " + cameraNames.toString());

        for(String cameraName: cameraNames){
            logger.debug("Adding camera " + cameraName);
            Webcam webcam = Webcam.getWebcams().stream().filter(i -> i.getName().contains("/dev/" + cameraName)).findFirst().orElse(null);
            if(webcam != null){
                Camera camera = new Camera(webcam);
                camera.start();
                cameras.add(camera);
            } else {
                logger.error("Camera " +cameraName + " not found");
            }
        }

        Object commandsObject = data.get("commands");
        if(commandsObject != null){
            ArrayList<Map<String, Object>> commands = (ArrayList<Map<String, Object>>) commandsObject;
            for(Map<String, Object> command: commands){
                deviceCommands.add(Interpreter.buildCommand(command));
            }
        }

        serialCom = new SerialCom(portName, baudRate);
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
    public void receiveMessage(ReceivedMessage receivedMessage){
        logger.debug("Received message: " + receivedMessage.getMessage());
        receivedMessages.add(receivedMessage); // todo: is this list even used?
        if(waitingForConformation && receivedMessage.getMessage().compareTo("done") == 0){
            logger.debug("Device is no longer waiting");
            waitingForConformation = false;
        }

        receivedMessageSubscriber.forEach(s -> s.update(receivedMessage));
    }

    public String getDeviceName(){
        return deviceName;
    }

    public String getPortName(){
        return portName;
    }

    public void sendMessage(String message) throws IOException {
        logger.debug("Sending message: " + message);
        serialCom.sendMessage(message + '\n' + '\r');
    }

    public ArrayList<ReceivedMessage> getReceivedMessagesList(){
        return receivedMessages;
    }

    public ArrayList<Camera> getCameras() {
        return cameras;
    }

    public ArrayList<DeviceCommand> getDeviceCommands() {
        return deviceCommands;
    }

    public void addDeviceInstruction(String instruction){
        deviceInstructions.push(instruction);
    }

    @Override
    public void addSubscriber(Subscriber subscriber) {
        receivedMessageSubscriber.add(subscriber);
    }

    @Override
    public void removeSubscriber(Subscriber subscriber) {
        receivedMessageSubscriber.remove(subscriber);
    }
}
