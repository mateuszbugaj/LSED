package Devices;

public class SerialPortNotFoundException extends Exception{
    public SerialPortNotFoundException(String serialPortName){
        super("Serial port " + serialPortName + " not found");
    }
}
