package Devices;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Supplier;

public class SerialCom {
    private static Logger logger = LoggerFactory.getLogger(SerialCom.class);
    private SerialPort serialPort;
    private String portName;

    public SerialCom(String serialPortName, Integer baudRate) throws SerialPortNotFoundException {
        if(serialPortName == null){ // todo: is this check necessary? It is handy for tests
            return;
        }
        logger.debug("Creating SerialCom for port: " + serialPortName);

        this.portName = serialPortName;
        this.serialPort = Arrays
                .stream(SerialPort.getCommPorts())
                .filter(i -> i.getSystemPortName().contains(serialPortName))
                .findFirst()
                .orElseThrow(new Supplier<SerialPortNotFoundException>() {
                    @Override
                    public SerialPortNotFoundException get() {
                        return new SerialPortNotFoundException(serialPortName);
                    }
                });

        try {
            serialPort.setBaudRate(baudRate);
            serialPort.openPort();

            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            InputStream inputStream = serialPort.getInputStream();
            if(inputStream.available() > 0){
                long skippedBytes = inputStream.skip(inputStream.available());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            System.out.println("Serial not selected");
        }
    }

    public String getPortName() {
        return portName;
    }

    public void addDataListener(SerialPortDataListener listener){
        if(serialPort != null){
            serialPort.addDataListener(listener);
        }
    }

    public void sendMessage(String message) throws IOException {
        if(message == null || message.isEmpty()){
            System.out.println("Cannot send empty message to the robot!");
        }

        if(serialPort != null){
            serialPort.getOutputStream().write(message.getBytes());
        }
    }
}