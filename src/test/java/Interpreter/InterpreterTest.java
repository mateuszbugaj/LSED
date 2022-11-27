package Interpreter;

import Devices.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class InterpreterTest {

    @Test
    public void buildCommandTest(){
        // Given
        DeviceCommandDTO commandDTO = new DeviceCommandDTO();
        commandDTO.setName("Move Relative");
        commandDTO.setDescription("Move device by distance of millimeters.");
        commandDTO.setPrefix("moveBy");
        commandDTO.setDevicePrefix("mv");

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

        commandDTO.setParams(List.of(param11, param12, param13));

        List<DeviceCommandParam> expectedParams = List.of(
                new DeviceCommandParam("XAxisDistance", DeviceCommandParamType.Integer, List.of(), -100, 100, false, null),
                new DeviceCommandParam("XAxisDistance", DeviceCommandParamType.Integer, List.of(), -100, 100, false, null),
                new DeviceCommandParam("XAxisDistance", DeviceCommandParamType.Integer, List.of(), -100, 100, false, null)
        );

        DeviceCommand expectedCommand = new DeviceCommand("Move Relative", "Move device by distance of millimeters.", "moveBy", "mv", expectedParams, List.of());

        // When
        DeviceCommand buildCommand = Interpreter.buildCommand(commandDTO);

        // Then
        Assertions.assertEquals(expectedCommand.getName(), buildCommand.getName());
        Assertions.assertEquals(expectedCommand.getDescription(), buildCommand.getDescription());
        Assertions.assertEquals(expectedCommand.getPrefix(), buildCommand.getPrefix());
        Assertions.assertEquals(expectedCommand.getDevicePrefix(), buildCommand.getDevicePrefix());
        Assertions.assertEquals(expectedCommand.getParams().size(), buildCommand.getParams().size());
        Assertions.assertEquals(expectedCommand.getParams().get(0).getName(), buildCommand.getParams().get(0).getName());
        Assertions.assertEquals(expectedCommand.getParams().get(0).getType(), buildCommand.getParams().get(0).getType());
        Assertions.assertEquals(expectedCommand.getParams().get(0).getOptional(), buildCommand.getParams().get(0).getOptional());
        Assertions.assertEquals(expectedCommand.getParams().get(0).getRangeMax(), buildCommand.getParams().get(0).getRangeMax());
        Assertions.assertEquals(expectedCommand.getParams().get(0).getRangeMin(), buildCommand.getParams().get(0).getRangeMin());
        Assertions.assertEquals(expectedCommand.getParams().get(0).getPredefined(), buildCommand.getParams().get(0).getPredefined());
        Assertions.assertEquals(expectedCommand.getParams().get(0).getPossibleValues(), buildCommand.getParams().get(0).getPossibleValues());
    }

//    @Test
//    void buildCommandTest() throws FileNotFoundException {
//        Yaml yaml = new Yaml();
//        String configFileName = "src/test/resources/deviceWithCommands.yaml";
//        Map<String, Object> data = yaml.load(new FileReader(configFileName));
//        ArrayList<Map<String, Object>> commands = (ArrayList<Map<String, Object> >) data.get("commands");
//
//        DeviceCommand expectedDeviceCommand1 = new DeviceCommand()
//                .setName("Move Relative")
//                .setPrefix("moveBy")
//                .setDevicePrefix("mv")
//                .setDescription("Move device by distance of millimeters.");
//
//        expectedDeviceCommand1.getParams().add(new DeviceCommandParam()
//                .setName("XAxisDistance")
//                .setType(DeviceCommandParamType.Integer)
//                .setRangeMin(-100)
//                .setRangeMax(100));
//
//        expectedDeviceCommand1.getParams().add(new DeviceCommandParam()
//                .setName("YAxisDistance")
//                .setType(DeviceCommandParamType.Integer)
//                .setRangeMin(-100)
//                .setRangeMax(100)
//                .setOptional(true));
//
//        expectedDeviceCommand1.getParams().add(new DeviceCommandParam()
//                .setName("ZAxisDistance")
//                .setType(DeviceCommandParamType.Integer)
//                .setRangeMin(-100)
//                .setRangeMax(100));
//
//        DeviceCommand expectedDeviceCommand2 = new DeviceCommand()
//                .setName("Light Switch")
//                .setPrefix("light")
//                .setDevicePrefix("lgt")
//                .setDescription("Switch bed light ON and OFF.");
//
//        expectedDeviceCommand2.getParams().add(new DeviceCommandParam()
//                .setName("State")
//                .setType(DeviceCommandParamType.String)
//                .addPossibleValue("on")
//                .addPossibleValue("off"));
//
//        DeviceCommand expectedDeviceCommand3 = new DeviceCommand()
//                .setName("Position 1")
//                .setPrefix("Position 1")
//                .setDevicePrefix("")
//                .setDescription("Move to the Position 1 one axis at the time.");
//        expectedDeviceCommand3.getEvents().addAll(List.of("mvto 100 0 0", "mvto 100 50 0", "mvto 100 50 20"));
//
//        assertEquals(expectedDeviceCommand1, Interpreter.buildCommand(commands.get(0)));
//        assertEquals(expectedDeviceCommand2, Interpreter.buildCommand(commands.get(1)));
//        assertEquals(expectedDeviceCommand3, Interpreter.buildCommand(commands.get(2)));
//    }
//
//    @Test
//    void interpretUserMessage() throws Throwable {
//        String content = Interpreter.COMMAND_PREFIX + "moveBy 10";
//        UserMessage userMessage = new UserMessage("User1", content, new Date());
//        ExternalDevice device = new ExternalDevice("src/test/resources/deviceWithCommands.yaml");
//        userMessage.setTargetDevice(device);
//
//        ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);// todo: this name is temporary
//        assertEquals("mv 10 0 0", deviceInstructions.get(0));
//    }
//
//    @Test
//    void interpretCombinedUserMessage() throws Throwable {
//        String content = Interpreter.COMMAND_PREFIX + "moveBy 10; moveBy 0 20";
//        UserMessage userMessage = new UserMessage("User1", content, new Date());
//        ExternalDevice device = new ExternalDevice("src/test/resources/deviceWithCommands.yaml");
//        userMessage.setTargetDevice(device);
//
//        ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);// todo: this name is temporary
//        assertEquals("mv 10 0 0", deviceInstructions.get(0));
//        assertEquals("mv 0 20 0", deviceInstructions.get(1));
//    }
//
//    @Test
//    void interpretUserMessageWithProvidedValues() throws Throwable {
//        String content = Interpreter.COMMAND_PREFIX + "light on";
//        UserMessage userMessage = new UserMessage("User1", content, new Date());
//        ExternalDevice device = new ExternalDevice("src/test/resources/deviceWithCommands.yaml");
//        userMessage.setTargetDevice(device);
//
//        ArrayList<String> deviceInstructions = Interpreter.interpret(userMessage);// todo: this name is temporary
//        assertEquals("lgt on", deviceInstructions.get(0));
//    }
//
//    @Test
//    void interpretUserMessageWithProvidedValuesButUsingDifferentOne() throws Throwable {
//        String content = Interpreter.COMMAND_PREFIX + "light dim";
//        UserMessage userMessage = new UserMessage("User1", content, new Date());
//        ExternalDevice device = new ExternalDevice("src/test/resources/deviceWithCommands.yaml");
//        userMessage.setTargetDevice(device);
//
//        assertThrows(RuntimeException.class, () -> Interpreter.interpret(userMessage));
//    }


}
