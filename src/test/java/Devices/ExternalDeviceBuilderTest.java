package Devices;

import View.Camera;
import View.CameraDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class ExternalDeviceBuilderTest {

    @Test
    public void builderTest() throws SerialPortNotFoundException {
        // Given
        ExternalDeviceDTO externalDeviceDTO = new ExternalDeviceDTO();
        externalDeviceDTO.setName("dev");
        externalDeviceDTO.setPortName("port");
        externalDeviceDTO.setPortBaudRate(1000);

        CameraDTO cameraDTO = new CameraDTO();
        cameraDTO.setName("cam1");
        cameraDTO.setPortName("/dev/cam1");
        externalDeviceDTO.setCameras(List.of(cameraDTO));

        DeviceCommandDTO commandDTO = new DeviceCommandDTO();
        commandDTO.setName("Command1");
        commandDTO.setPrefix("Com1");
        externalDeviceDTO.setCommands(List.of(commandDTO));

        // When
        ExternalDeviceBuilder externalDeviceBuilder = spy(ExternalDeviceBuilder.class);
        doReturn(externalDeviceBuilder).when(externalDeviceBuilder).setSerialCom(externalDeviceDTO.getPortName(), externalDeviceDTO.getPortBaudRate());

        ExternalDevice device = externalDeviceBuilder
                .setDeviceName(externalDeviceDTO.getName())
                .setSerialCom(externalDeviceDTO.getPortName(), externalDeviceDTO.getPortBaudRate())
                .setCameras(externalDeviceDTO.getCameras())
                .setCommands(externalDeviceDTO.getCommands())
                .build();

        // Then
        Assertions.assertEquals(externalDeviceDTO.getName(), device.getName());
        Assertions.assertEquals(
                externalDeviceDTO.getCameras().stream().map(CameraDTO::getPortName).collect(Collectors.toList()),
                device.getCameras().stream().map(Camera::getPortName).collect(Collectors.toList()));
//        Assertions.assertEquals(externalDeviceDTO.getCommands().get(0).getName(), device.getDeviceCommands().get(0).getName());

    }

    @Test
    public void builderCameraReuseTest(){
        // Given
        ExternalDeviceDTO externalDeviceDTO1 = new ExternalDeviceDTO();
        externalDeviceDTO1.setName("dev1");
        externalDeviceDTO1.setPortName("port1");

        CameraDTO cameraDTO1 = new CameraDTO();
        cameraDTO1.setName("cam1");
        cameraDTO1.setPortName("/dev/cam");
        externalDeviceDTO1.setCameras(List.of(cameraDTO1));

        ExternalDeviceDTO externalDeviceDTO2 = new ExternalDeviceDTO();
        externalDeviceDTO2.setName("dev2");
        externalDeviceDTO2.setPortName("port2");

        CameraDTO cameraDTO2 = new CameraDTO();
        cameraDTO2.setName("cam2");
        cameraDTO2.setPortName("/dev/cam");

        CameraDTO cameraDTO3 = new CameraDTO();
        cameraDTO3.setName("cam3");
        cameraDTO3.setPortName("/dev/cam3");
        externalDeviceDTO2.setCameras(List.of(cameraDTO2, cameraDTO3));

        // When
        ExternalDeviceBuilder externalDeviceBuilder = new ExternalDeviceBuilder();
        ExternalDevice build1 = externalDeviceBuilder.setDeviceName(externalDeviceDTO1.getName()).setCameras(externalDeviceDTO1.getCameras()).build();
        ExternalDevice build2 = externalDeviceBuilder.setDeviceName(externalDeviceDTO2.getName()).setCameras(externalDeviceDTO2.getCameras()).build();

        // Then
        Assertions.assertEquals(build1.getCameras().get(0), build2.getCameras().get(0));
        Assertions.assertNotEquals(build1.getCameras().get(0), build2.getCameras().get(1));
    }
}
