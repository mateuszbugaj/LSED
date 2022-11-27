package Devices;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SerialPortDataListenerImplTest {
    SerialPortDataListenerImpl dataListener;
    ExternalDevice device;

    @BeforeEach
    public void setup() throws Throwable {
        device = new ExternalDevice("dev1", new SerialCom("port1", 9600), List.of(), List.of());
        dataListener = new SerialPortDataListenerImpl(device);
    }

    @Test
    public void receiveOneLineMessage() throws Throwable {
        String msg = "xxx\n";
        String msgWithCaretReturn = msg + '\r';
        dataListener.receivedMessage = msgWithCaretReturn;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, msgWithCaretReturn.getBytes()));

        assertEquals(msg.trim(), device.getReceivedMessagesList().get(0).getMessage());
    }

    @Test
    public void receiveTwoLineMessage() throws Throwable {
        String msg = "xxx\nyyy\n";
        String msgWithCaretReturn = msg + '\r';
        dataListener.receivedMessage = msgWithCaretReturn;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, msgWithCaretReturn.getBytes()));

        assertEquals(msg.trim(), device.getReceivedMessagesList().get(0).getMessage());
    }

    @Test
    public void receiveMessageSplitIntoTwoChunks() throws Throwable {
        String chunk1 = "Part one\n";
        String chunk2 = "Part two\n";
        String completeMsg = chunk1.concat(chunk2);
        String chunk2WithCaretReturn = chunk2 + '\r';
        dataListener.receivedMessage = chunk1;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunk1.getBytes()));

        dataListener.receivedMessage = chunk2WithCaretReturn;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunk2WithCaretReturn.getBytes()));

        assertEquals(completeMsg.trim(), device.getReceivedMessagesList().get(0).getMessage());
    }

    @Test
    public void receiveMessageSplitIntoThreeChunks() throws Throwable {
        String chunk1 = "Part one\n";
        String chunk2 = "Part two\n";
        String chunk3 = "Part three\n";
        String completeMsg = chunk1.concat(chunk2).concat(chunk3);
        String chunk3WithCaretReturn = chunk3 + '\r';

        dataListener.receivedMessage = chunk1;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunk1.getBytes()));

        dataListener.receivedMessage = chunk2;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunk2.getBytes()));

        dataListener.receivedMessage = chunk3WithCaretReturn;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunk3WithCaretReturn.getBytes()));

        assertEquals(completeMsg.trim(), device.getReceivedMessagesList().get(0).getMessage());
    }

    @Test
    public void receiveTwoMessages() throws Throwable {
        String msgOne = "Message one\n";
        String msgTwo = "Message two\n";
        String msgOneWithCaretReturn = msgOne + '\r';
        String msgTwoWithCaretReturn = msgTwo + '\r';

        dataListener.receivedMessage = msgOneWithCaretReturn;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, msgOneWithCaretReturn.getBytes()));

        dataListener.receivedMessage = msgTwoWithCaretReturn;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, msgTwoWithCaretReturn.getBytes()));

        assertEquals(msgOne.trim(), device.getReceivedMessagesList().get(0).getMessage());
        assertEquals(msgTwo.trim(), device.getReceivedMessagesList().get(1).getMessage());
    }

    @Test
    public void receiveTwoMessagesSplitUnevenlyIntoTwoChunks() throws Throwable {
        String chunkOne = "Message one\n\rMessage ";
        String chunkTwo = "two\n\r";

        dataListener.receivedMessage = chunkOne;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunkOne.getBytes()));

        dataListener.receivedMessage = chunkTwo;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunkTwo.getBytes()));

        assertEquals("Message one", device.getReceivedMessagesList().get(0).getMessage());
        assertEquals("Message two", device.getReceivedMessagesList().get(1).getMessage());
    }

    @Test
    public void receiveThreeMessagesSplitUnevenlyIntoTwoChunks() throws Throwable {
        String chunkOne = "Message one\n\rMessage two\n\rMessage ";
        String chunkTwo = "three\n\r";

        dataListener.receivedMessage = chunkOne;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunkOne.getBytes()));

        dataListener.receivedMessage = chunkTwo;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunkTwo.getBytes()));

        assertEquals("Message one", device.getReceivedMessagesList().get(0).getMessage());
        assertEquals("Message two", device.getReceivedMessagesList().get(1).getMessage());
        assertEquals("Message three", device.getReceivedMessagesList().get(2).getMessage());
    }

    @Test
    public void practicalTestOne() throws Throwable {
        String chunkOne = "Got lgt on\n\rLights on\n\rBrightnes";
        String chunkTwo = "s: 64\n\r";
        String chunkThree = "done\n\r";

        dataListener.receivedMessage = chunkOne;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunkOne.getBytes()));

        dataListener.receivedMessage = chunkTwo;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunkTwo.getBytes()));

        dataListener.receivedMessage = chunkThree;
        dataListener.serialEvent(new SerialPortEvent(SerialPort.getCommPorts()[0], SerialPort.LISTENING_EVENT_DATA_AVAILABLE, chunkThree.getBytes()));

        assertEquals("Got lgt on", device.getReceivedMessagesList().get(0).getMessage());
        assertEquals("Lights on", device.getReceivedMessagesList().get(1).getMessage());
        assertEquals("Brightness: 64", device.getReceivedMessagesList().get(2).getMessage());
        assertEquals("done", device.getReceivedMessagesList().get(3).getMessage());
    }

}