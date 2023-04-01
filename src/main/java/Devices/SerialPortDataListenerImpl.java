package Devices;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class SerialPortDataListenerImpl implements SerialPortDataListener {
    private final int CUTOFF_ASCII = '\r';
    private static final Logger logger = LoggerFactory.getLogger(SerialPortDataListenerImpl.class);
    private final ExternalDevice device;
    private String buffer = "";
    public String receivedMessage = ""; // todo: this should be mocked

    public SerialPortDataListenerImpl(ExternalDevice device) {
        this.device = device;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        SerialPort serialPort = serialPortEvent.getSerialPort();

        // todo: mock work-around
        if(receivedMessage.equals("")){
            int bytesAvailable = serialPort.bytesAvailable();
            byte[] byteBuffer = new byte[bytesAvailable];
            serialPort.readBytes(byteBuffer, bytesAvailable);

            receivedMessage = new String(byteBuffer);
        }

//        logger.debug("Got data chunk: " + receivedMessage.replace("\r", "\\r").replace("\n", "\\n")); // todo: fix this so it doesn't send every char
        if(receivedMessage.indexOf('\r') != -1){
            receivedMessage = buffer.concat(receivedMessage);
            buffer = "";

            long fullMessageCount = receivedMessage.chars().filter(c -> c == '\r').count();
            String[] messageSplit = receivedMessage.split("\r");
            for(int i = 0; i < messageSplit.length; i++){
                if(i < fullMessageCount){
                    device.receiveMessage(new ReceivedMessage(device.getName(), messageSplit[i].trim(), new Date()));
                } else {
                    buffer = buffer.concat(messageSplit[i]);
                }
            }
        } else {
            buffer = buffer.concat(receivedMessage);
        }

        receivedMessage = "";
    }
}
