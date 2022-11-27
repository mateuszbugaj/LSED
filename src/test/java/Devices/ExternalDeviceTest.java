package Devices;

import View.CameraDTO;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;

import java.io.*;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ExternalDeviceTest {

//    private ExternalDeviceBuilderDirector builderDirector;
//    private ExternalDeviceBuilder deviceBuilder;

    @Before
    public void setup(){
//        deviceBuilder = spy(ExternalDeviceBuilder.class);
//        builderDirector = new ExternalDeviceBuilderDirector(deviceBuilder);
    }

    @Test
    public void buildingExternalDeviceTest() throws IOException, SerialPortNotFoundException {
        // given
        ExternalDeviceBuilder deviceBuilderSpy = spy(ExternalDeviceBuilder.class);
        ExternalDeviceBuilderDirector builderDirector = new ExternalDeviceBuilderDirector(deviceBuilderSpy);

        ExternalDeviceDTO externalDeviceDTO = new ExternalDeviceDTO();

        String deviceName = "dev1";
        externalDeviceDTO.setName(deviceName);

        String portName = "ttyUSB0";
        externalDeviceDTO.setPortName(portName);

        Integer portBaudRate = 9600;
        externalDeviceDTO.setPortBaudRate(portBaudRate);

        CameraDTO camera1 = new CameraDTO();
        camera1.setName("cam1");
        camera1.setPortName("/dev/cam1");

        CameraDTO camera2 = new CameraDTO();
        camera2.setName("cam2");
        camera2.setPortName("/dev/cam2");

        CameraDTO camera3 = new CameraDTO();
        camera3.setName("cam3");
        camera3.setPortName("/dev/cam3");
        externalDeviceDTO.setCameras(List.of(camera1, camera2, camera3));

        // when
        doReturn(deviceBuilderSpy).when(deviceBuilderSpy).setSerialCom(portName, portBaudRate);
        ExternalDevice device = builderDirector.build(externalDeviceDTO);

        // then
        Assertions.assertEquals(deviceName, device.getName());
//        Assertions.assertEquals(portName, device.getPortName()); // todo: think how to mock the PortCom to return the port name
        Assertions.assertEquals(externalDeviceDTO.getCameras().size(), device.getCameras().size());
    }

//    @Test
//    public void throwSerialPortNotFoundExceptionWhenSerialPortIsNotFound() {
//        Exception exception = Assertions.assertThrows(SerialPortNotFoundException.class, () -> {
//            new ExternalDevice("src/test/resources/exampleDevice.yaml");
//        });
//
//        String portName = "ttyUSBExample"; // todo: put this into the file and not only read the same thing
//        String expectedMessage = "Serial port " + portName + " not found"; // todo: make it more robust
//
//        Assertions.assertTrue(exception.getMessage().contains(expectedMessage));
//    }
//
//    @Test
//    public void createMinimalDeviceFromConfigFile() throws Throwable {
//        ExternalDevice device = new ExternalDevice("src/test/resources/exampleDeviceMinimal.yaml");
//
//        Assertions.assertEquals("Example", device.getDeviceName());
//    }
//
//    @Test
//    public void testReceivingMessageByTheDevice() throws Throwable {
//        ExternalDevice device = new ExternalDevice("src/test/resources/exampleDeviceMinimal.yaml");
//
//        Subscriber<ReceivedMessage> subscriber = new Subscriber<ReceivedMessage>() {
//            ReceivedMessage content;
//
//            @Override
//            public void update(ReceivedMessage content) {
//                this.content = content;
//            }
//        };
//
//        device.addSubscriber(subscriber);
//        // todo: how to simulate receiving message?
//    }
}
