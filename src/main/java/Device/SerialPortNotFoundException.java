package Device;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortNotFoundException extends Exception{
    public SerialPortNotFoundException(String serialPortName){
        super("Serial port " + serialPortName + " not found");
    }
}
