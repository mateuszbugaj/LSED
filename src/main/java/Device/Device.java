package Device;

import Utils.Subscriber;
import View.Camera;
import Utils.Publisher;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.github.sarxos.webcam.Webcam;
import org.yaml.snakeyaml.Yaml;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

public class Device implements Publisher {
    private final String deviceName;
    private final String portName;
    private final SerialCom serialCom;
    private final String configFile; // todo: Path type?
    private final ArrayList<ReceivedMessage> receivedMessages = new ArrayList<>();
    private final ArrayList<Subscriber<ReceivedMessage>> receivedMessageSubscriber = new ArrayList<>();
    private final ArrayList<Camera> cameras = new ArrayList<>();

    public Device(String configFile) throws IOException {
        this.configFile = configFile;

        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(new FileReader(configFile));

        deviceName = (String) data.get("name");
        portName = (String) data.get("portname");
        Integer baudrate = (Integer) data.get("baudrate");

        ArrayList<String> cameraNames = (ArrayList<String>) data.get("cameras");
        if(cameraNames !=null){
            for(String cameraName: cameraNames){
                System.out.println(cameraName);
                Webcam webcam = Webcam.getWebcams().stream().filter(i -> i.getName().contains("/dev/" + cameraName)).findFirst().orElseThrow();
                Camera camera = new Camera(webcam);
                camera.start();
                cameras.add(camera);
            }
        } else {
            System.out.println("No cameras defined for: " + deviceName);
        }

        Object commandsObject = data.get("commands");
        if(commandsObject != null){
            System.out.println((ArrayList<String>) commandsObject);
        }

        SerialPort serialPort = Arrays
                .stream(SerialPort.getCommPorts())
                .filter(i -> i.getSystemPortName().contains(portName))
                .findFirst()
                .orElseThrow();

        serialCom = new SerialCom(serialPort, baudrate);

        serialPort.addDataListener(new SerialPortDataListener() {
            private final int CUTOFF_ASCII = '\r';
            private String connectedMessage = "";

            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent serialPortEvent) {
                SerialPort serialPort = serialPortEvent.getSerialPort();
                String buffer = getBuffer(serialPort);
                connectedMessage = connectedMessage.concat(buffer);

                if((connectedMessage.indexOf(CUTOFF_ASCII) + 1) > 0) {
                    String outputString = connectedMessage
                            .substring(0, connectedMessage.indexOf(CUTOFF_ASCII) + 1)
                            .replace("\n", "")
                            .replace("\r", "");

                    connectedMessage = connectedMessage.substring(connectedMessage.indexOf(CUTOFF_ASCII) + 1);
                    ReceivedMessage receivedMessage = new ReceivedMessage(outputString, new Timestamp(new Date().getTime()));
                    receivedMessages.add(receivedMessage); // todo: is this list even used?
                    receivedMessageSubscriber.forEach(s -> s.update(receivedMessage));
                }
            }

            protected String getBuffer(SerialPort serialPort){
                int bytesAvailable = serialPort.bytesAvailable();
                byte[] buffer = new byte[bytesAvailable];
                serialPort.readBytes(buffer, bytesAvailable);

                return new String(buffer);
            }
        });
    }

    public String getDeviceName(){
        return deviceName;
    }

    public String getPortName(){
        return portName;
    }

    public void sendMessage(String message) throws IOException {
        System.out.println("Sending: " + message);
        serialCom.sendMessage(message + '\n' + '\r');
    }

    public ArrayList<ReceivedMessage> getReceivedMessagesList(){
        return receivedMessages;
    }

    public ArrayList<Camera> getCameras() {
        return cameras;
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
