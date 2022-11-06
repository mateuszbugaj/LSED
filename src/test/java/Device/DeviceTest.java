package Device;

import Utils.Subscriber;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceTest {

    @Test
    public void throwSerialPortNotFoundExceptionWhenSerialPortIsNotFound() {
        Exception exception = assertThrows(SerialPortNotFoundException.class, () -> {
            new Device("src/test/resources/exampleDevice.yaml");
        });

        String portName = "ttyUSBExample"; // todo: put this into the file and not only read the same thing
        String expectedMessage = "Serial port " + portName + " not found"; // todo: make it more robust

        assertTrue(exception.getMessage().contains(expectedMessage));
    }

    @Test
    public void createMinimalDeviceFromConfigFile() throws Throwable {
        Device device = new Device("src/test/resources/exampleDeviceMinimal.yaml");

        assertEquals("Example", device.getDeviceName());
    }

    @Test
    public void testReceivingMessageByTheDevice() throws Throwable {
        Device device = new Device("src/test/resources/exampleDeviceMinimal.yaml");

        Subscriber<ReceivedMessage> subscriber = new Subscriber<ReceivedMessage>() {
            ReceivedMessage content;

            @Override
            public void update(ReceivedMessage content) {
                this.content = content;
            }
        };

        device.addSubscriber(subscriber);
        // todo: how to simulate receiving message?
    }
}
