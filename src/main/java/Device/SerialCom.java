package Device;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.function.Supplier;

public class SerialCom {
    private SerialPort serialPort;

    public SerialCom(String serialPortName, Integer baudRate) throws SerialPortNotFoundException {
        if(serialPortName == null){
            return;
        }

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