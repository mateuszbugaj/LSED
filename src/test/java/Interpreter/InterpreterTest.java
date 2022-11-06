package Interpreter;

import Device.Device;
import Device.DeviceCommand;
import Device.DeviceCommandParam;
import Device.DeviceCommandParamType;
import StreamingService.UserMessage;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class InterpreterTest {

    @Test
    void buildCommandTest() throws FileNotFoundException {
        Yaml yaml = new Yaml();
        String configFileName = "src/test/resources/deviceWithCommands.yaml";
        Map<String, Object> data = yaml.load(new FileReader(configFileName));
        ArrayList<Map<String, Object>> commands = (ArrayList<Map<String, Object> >) data.get("commands");

        DeviceCommand expectedDeviceCommand1 = new DeviceCommand()
                .setName("Move Relative")
                .setPrefix("moveBy")
                .setDevicePrefix("mv")
                .setDescription("Move device by distance of millimeters.");

        expectedDeviceCommand1.getParams().add(new DeviceCommandParam()
                .setName("XAxisDistance")
                .setType(DeviceCommandParamType.Integer)
                .setRangeMin(-100)
                .setRangeMax(100));

        expectedDeviceCommand1.getParams().add(new DeviceCommandParam()
                .setName("YAxisDistance")
                .setType(DeviceCommandParamType.Integer)
                .setRangeMin(-100)
                .setRangeMax(100)
                .setOptional(true));

        expectedDeviceCommand1.getParams().add(new DeviceCommandParam()
                .setName("ZAxisDistance")
                .setType(DeviceCommandParamType.Integer)
                .setRangeMin(-100)
                .setRangeMax(100));

        DeviceCommand expectedDeviceCommand2 = new DeviceCommand()
                .setName("Light Switch")
                .setPrefix("light")
                .setDevicePrefix("lgt")
                .setDescription("Switch bed light ON and OFF.");

        expectedDeviceCommand2.getParams().add(new DeviceCommandParam()
                .setName("State")
                .setType(DeviceCommandParamType.String)
                .addPossibleValue("on")
                .addPossibleValue("off"));

        DeviceCommand expectedDeviceCommand3 = new DeviceCommand()
                .setName("Position 1")
                .setPrefix("Position 1")
                .setDevicePrefix("")
                .setDescription("Move to the Position 1 one axis at the time.");
        expectedDeviceCommand3.getEvents().addAll(List.of("mvto 100 0 0", "mvto 100 50 0", "mvto 100 50 20"));

        assertEquals(expectedDeviceCommand1, Interpreter.buildCommand(commands.get(0)));
        assertEquals(expectedDeviceCommand2, Interpreter.buildCommand(commands.get(1)));
        assertEquals(expectedDeviceCommand3, Interpreter.buildCommand(commands.get(2)));
    }

    @Test
    void interpretUserMessage() throws Throwable {
        String content = Interpreter.COMMAND_PREFIX + "moveBy 10";
        UserMessage userMessage = new UserMessage("User1", content, new Date());
        Device device = new Device("src/test/resources/deviceWithCommands.yaml");
        userMessage.setTargetDevice(device);

        ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);// todo: this name is temporary
        assertEquals("mv 10 0 0", deviceInstructions.get(0));
    }

    @Test
    void interpretCombinedUserMessage() throws Throwable {
        String content = Interpreter.COMMAND_PREFIX + "moveBy 10; moveBy 0 20";
        UserMessage userMessage = new UserMessage("User1", content, new Date());
        Device device = new Device("src/test/resources/deviceWithCommands.yaml");
        userMessage.setTargetDevice(device);

        ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);// todo: this name is temporary
        assertEquals("mv 10 0 0", deviceInstructions.get(0));
        assertEquals("mv 0 20 0", deviceInstructions.get(1));
    }

    @Test
    void interpretUserMessageWithProvidedValues() throws Throwable {
        String content = Interpreter.COMMAND_PREFIX + "light on";
        UserMessage userMessage = new UserMessage("User1", content, new Date());
        Device device = new Device("src/test/resources/deviceWithCommands.yaml");
        userMessage.setTargetDevice(device);

        ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);// todo: this name is temporary
        assertEquals("lgt on", deviceInstructions.get(0));
    }

    @Test
    void interpretUserMessageWithProvidedValuesButUsingDifferentOne() throws Throwable {
        String content = Interpreter.COMMAND_PREFIX + "light dim";
        UserMessage userMessage = new UserMessage("User1", content, new Date());
        Device device = new Device("src/test/resources/deviceWithCommands.yaml");
        userMessage.setTargetDevice(device);

        assertThrows(RuntimeException.class, () -> Interpreter.interpret(userMessage));
    }
}
