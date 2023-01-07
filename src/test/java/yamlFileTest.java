import Devices.DeviceCommandDTO;
import Devices.DeviceCommandParamDTO;
import Devices.DeviceCommandParamType;
import Devices.ExternalDeviceDTO;
import View.CameraDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class yamlFileTest {
    private static String resourceDirectory = "src/test/resources/";

    @Test
    void deviceDtoFromYamlTest() throws FileNotFoundException {
        // Given
        ExternalDeviceDTO expectedDto = new ExternalDeviceDTO();
        expectedDto.setName("dev1");
        expectedDto.setPortName("port1");
        expectedDto.setPortBaudRate(1000);
        expectedDto.setInitialState("Start");

        CameraDTO camera1 = new CameraDTO();
        camera1.setName("cam1");
        camera1.setPortName("/dev/cam1");

        CameraDTO camera2 = new CameraDTO();
        camera2.setName("cam2");
        camera2.setPortName("/dev/cam2");

        CameraDTO camera3 = new CameraDTO();
        camera3.setName("cam3");
        camera3.setPortName("/dev/cam3");
        expectedDto.setCameras(List.of(camera1, camera2, camera3));

        DeviceCommandDTO command1 = new DeviceCommandDTO();
        command1.setName("Move Relative");
        command1.setDescription("Move device by distance of millimeters.");
        command1.setPrefix("moveBy");
        command1.setDevicePrefix("mv");
        command1.setRequiredStates(List.of("Home", "Position_1"));
        command1.setResultingState("Moved");

        DeviceCommandParamDTO param11 = new DeviceCommandParamDTO();
        param11.setName("XAxisDistance");
        param11.setType(DeviceCommandParamType.Integer);
        param11.setRange(List.of(-100, 100));

        DeviceCommandParamDTO param12 = new DeviceCommandParamDTO();
        param12.setName("YAxisDistance");
        param12.setType(DeviceCommandParamType.Integer);
        param12.setRange(List.of(-100, 100));
        param12.setOptional(true);
        param12.setPredefined(String.valueOf(0));

        DeviceCommandParamDTO param13 = new DeviceCommandParamDTO();
        param13.setName("ZAxisDistance");
        param13.setType(DeviceCommandParamType.Integer);
        param13.setRange(List.of(-100, 100));
        param13.setOptional(true);
        param13.setPredefined(String.valueOf(0));

        command1.setParams(List.of(param11, param12, param13));

        DeviceCommandDTO command2 = new DeviceCommandDTO();
        command2.setName("Light Switch");
        command2.setDescription("Switch bed light ON and OFF.");
        command2.setPrefix("light");
        command2.setDevicePrefix("lgt");

        DeviceCommandParamDTO param21 = new DeviceCommandParamDTO();
        param21.setName("State");
        param21.setType(DeviceCommandParamType.String);
        param21.setValues(List.of("on", "off"));

        DeviceCommandParamDTO param22 = new DeviceCommandParamDTO();
        param22.setName("Intensity");
        param22.setType(DeviceCommandParamType.Integer);
        param22.setRange(List.of(100));
        param22.setOptional(true);

        command2.setParams(List.of(param21, param22));

        DeviceCommandDTO command3 = new DeviceCommandDTO();
        command3.setName("Home Manipulator");
        command3.setDescription("Move Manipulator to its Home position in all axes.");
        command3.setPrefix("home");
        command3.setDevicePrefix("G28");
        command3.setRequiredStates(List.of("Start", "Home", "Position_1", "Moved"));
        command3.setResultingState("Home");

        DeviceCommandDTO command4 = new DeviceCommandDTO();
        command4.setName("Position 1");
        command4.setDescription("Move to the Position 1 one axis at the time.");
        command4.setPrefix("Position_1");
        command4.setEvents(List.of("mvto 100 0 0", "mvto 100 50 0", "mvto 100 50 20"));
        command4.setRequiredStates(List.of("Home"));
        command4.setResultingState("Position_1");

        expectedDto.setCommands(List.of(command1, command2, command3, command4));

        // When
        Yaml yaml = new Yaml(new Constructor(ExternalDeviceDTO.class));
        String configFileName = resourceDirectory + "yamlTest1.yaml";
        ExternalDeviceDTO deviceDto = yaml.load(new FileReader(configFileName));

        // Then
        Assertions.assertEquals(0, deviceDto.compareTo(expectedDto));
    }
}
