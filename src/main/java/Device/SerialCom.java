package Device;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;

public class SerialCom {
    private final SerialPort serialPort;

    public SerialCom(SerialPort serialPort, Integer baudrate) {
        this.serialPort = serialPort;

        try {
            serialPort.setBaudRate(baudrate);
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

    public void sendMessage(String message) throws IOException {
        if(message == null || message.isEmpty()){
            System.out.println("Cannot send empty message to the robot!");
        }

        if(serialPort != null){
            serialPort.getOutputStream().write(message.getBytes());
        }
    }
}

//class SerialComEventHandler implements SerialPortDataListener {
//    private final int CUTOFF_ASCII = 10; // Line feed character
//    private String connectedMessage = "";
//    private final ArrayList<Subscriber> subscribers;
//
//    public SerialComEventHandler(ArrayList<Subscriber> subscribers) {
//        this.subscribers = subscribers;
//    }
//
//    @Override
//    public int getListeningEvents() {
//        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
//    }
//
//    @Override
//    public void serialEvent(SerialPortEvent serialPortEvent) {
//        SerialPort serialPort = serialPortEvent.getSerialPort();
//        String buffer = getBuffer(serialPort);
//        connectedMessage = connectedMessage.concat(buffer);
//
//        if((connectedMessage.indexOf(CUTOFF_ASCII) + 1) > 0) {
//            String outputString = connectedMessage
//                    .substring(0, connectedMessage.indexOf(CUTOFF_ASCII) + 1)
//                    .replace("\n", "");
//
//            connectedMessage = connectedMessage.substring(connectedMessage.indexOf(CUTOFF_ASCII) + 1);
//            System.out.println("Got: " + outputString);
//            subscribers.forEach(s -> s.update(outputString));
//        }
//    }
//
//    protected String getBuffer(SerialPort serialPort){
//        int bytesAvailable = serialPort.bytesAvailable();
//        byte[] buffer = new byte[bytesAvailable];
//        serialPort.readBytes(buffer, bytesAvailable);
//
//        return new String(buffer);
//    }
//}