package Interpreter;

import Devices.*;
import StreamingService.Message;
import StreamingService.UserManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

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

        DeviceCommand expectedCommand = new DeviceCommand("Move Relative", "Move device by distance of millimeters.", "moveBy", "mv", expectedParams, List.of(), List.of(), "");

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

    @Test
    public void interpretCommandWithOneIntegerParameterTest() throws Throwable {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1", "CM1", "cm_1", List.of(param1), List.of(), List.of("State1", "State2"), "State3");
        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand), "State1");
        Message userMessage = new Message(userManager.getUser("User1"), "!dev1 CM1 50", new Date());
        userMessage.setTargetDevice(externalDevice);

        List<DeviceCommand> deviceCommands = Interpreter.interpret(userMessage);

        Assertions.assertEquals(List.of("cm_1 50"), deviceCommands.get(0).getDeviceInstructions());
    }

    @Test
    public void interpretCommandWithOneIntegerParameterAndOneStringParameterTest() throws Throwable {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "");
        DeviceCommandParam param2 = new DeviceCommandParam("Param2", DeviceCommandParamType.String, List.of("ABC", "XXX"), null, null, false, "");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1", "CM1", "cm_1", List.of(param1, param2), List.of(), List.of("State1", "State2"), "State3");
        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand), "State1");
        Message userMessage = new Message(userManager.getUser("User1"), "!dev1 CM1 50 ABC", new Date());
        userMessage.setTargetDevice(externalDevice);

        List<DeviceCommand> deviceCommands = Interpreter.interpret(userMessage);

        Assertions.assertEquals(List.of("cm_1 50 ABC"), deviceCommands.get(0).getDeviceInstructions());
    }

    @Test
    public void interpretCommandWithOneIntegerParameterAndOneStringParameterAndTwoCommandsWithTheSameNameAndNumberOfParametersTest() throws Throwable {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "");
        DeviceCommandParam param2 = new DeviceCommandParam("Param2", DeviceCommandParamType.String, List.of("ABC", "XXX"), null, null, false, "");
        DeviceCommand deviceCommand1 = new DeviceCommand("Command 1", "Command 1 (Integer, String)", "CM1", "cm_1", List.of(param1, param2), List.of(), List.of("State1", "State2"), "State3");
        DeviceCommand deviceCommand2 = new DeviceCommand("Command 1", "Command 1 (Integer, Integer)", "CM1", "cm_1", List.of(param1, param1), List.of(), List.of("State1", "State2"), "State3");

        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand1, deviceCommand2), "State1");
        Message userMessage1 = new Message(userManager.getUser("User1"), "!dev1 CM1 50 80", new Date());
        userMessage1.setTargetDevice(externalDevice);

        Message userMessage2 = new Message(userManager.getUser("User1"), "!dev1 CM1 50 XXX", new Date());
        userMessage2.setTargetDevice(externalDevice);

        Assertions.assertEquals(List.of("cm_1 50 80"), Interpreter.interpret(userMessage1).get(0).getDeviceInstructions());
        Assertions.assertEquals(List.of("cm_1 50 XXX"), Interpreter.interpret(userMessage2).get(0).getDeviceInstructions());
    }

    @Test
    public void interpretAMessageContainingCommandWithTheSameNameAndDifferentNumberOfParametersTest() throws Throwable {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "");
        DeviceCommandParam param2 = new DeviceCommandParam("Param2", DeviceCommandParamType.String, List.of("ABC", "XXX"), null, null, false, "");
        DeviceCommand deviceCommand1 = new DeviceCommand("Command 1", "Command 1 (Integer, String)", "CM1", "cm_1", List.of(param1, param2), List.of(), List.of("State1", "State2"), "State3");
        DeviceCommand deviceCommand2 = new DeviceCommand("Command 1", "Command 1 (Integer)", "CM1", "cm_1", List.of(param1), List.of(), List.of("State1", "State2"), "State3");

        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand1, deviceCommand2), "State1");
        Message userMessage1 = new Message(userManager.getUser("User1"), "!dev1 CM1 50 ABC", new Date());
        userMessage1.setTargetDevice(externalDevice);

        Message userMessage2 = new Message(userManager.getUser("User1"), "!dev1 CM1 50", new Date());
        userMessage2.setTargetDevice(externalDevice);

        Assertions.assertEquals(List.of("cm_1 50 ABC"), Interpreter.interpret(userMessage1).get(0).getDeviceInstructions());
        Assertions.assertEquals(List.of("cm_1 50"), Interpreter.interpret(userMessage2).get(0).getDeviceInstructions());
    }

    @Test
    public void interpretCommandWithEventsTest() throws Throwable {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1", "CM1", "cm_1", List.of(), List.of("ABC", "XXX", "YYY"), List.of("State1", "State2"), "State3");
        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand), "State1");
        Message userMessage = new Message(userManager.getUser("User1"), "!dev1 CM1", new Date());
        userMessage.setTargetDevice(externalDevice);

        List<DeviceCommand> deviceCommands = Interpreter.interpret(userMessage);

        Assertions.assertEquals(List.of("ABC", "XXX", "YYY"), deviceCommands.get(0).getDeviceInstructions());
    }

    @Test
    public void interpretCommandForDeviceWithIncorrectInitialStateTest() {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1", "CM1", "cm_1", List.of(param1), List.of(), List.of("State1", "State2"), "State3");
        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand), "State5");
        Message userMessage = new Message(userManager.getUser("User1"), "!dev1 CM1 50", new Date());
        userMessage.setTargetDevice(externalDevice);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            Interpreter.interpret(userMessage);
        });

        Assertions.assertEquals("Device needs to be in the state: [State1, State2]", exception.getMessage());
    }

    @Test
    public void interpretCommandWithIncorrectSignatureTest() {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1", "CM1", "cm_1", List.of(param1), List.of(), List.of("State1", "State2"), "State3");
        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand), "State1");
        Message userMessage = new Message(userManager.getUser("User1"), "!dev1 CM1 ABC", new Date());
        userMessage.setTargetDevice(externalDevice);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            List<DeviceCommand> deviceCommands = Interpreter.interpret(userMessage);
        });

        Assertions.assertEquals("No matching command found for the device.", exception.getMessage());
    }

    @Test
    public void interpretCommandWithOptionalParametersTest() throws Throwable {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.Integer, List.of(), 0, 100, false, "");
        DeviceCommandParam param2 = new DeviceCommandParam("Param2", DeviceCommandParamType.Integer, List.of(), 0, 100, true, "0");
        DeviceCommandParam param3 = new DeviceCommandParam("Param3", DeviceCommandParamType.Integer, List.of(), 0, 100, true, "0");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1", "CM1", "cm_1", List.of(param1, param2, param3), List.of(), List.of("State1", "State2"), "State3");
        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand), "State1");
        Message userMessage = new Message(userManager.getUser("User1"), "!dev1 CM1 10", new Date());
        userMessage.setTargetDevice(externalDevice);

        List<DeviceCommand> deviceCommands = Interpreter.interpret(userMessage);
        Assertions.assertEquals(List.of("cm_1 10 0 0"), deviceCommands.get(0).getDeviceInstructions());
    }

    @Test
    public void interpretCommandWithParametersNotFromTheListTest() {
        UserManager userManager = new UserManager(List.of(), List.of());

        List<String> possibleValues = List.of("ABC", "XXX");
        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.String, possibleValues, 0, 0, false, "");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1", "CM1", "cm_1", List.of(param1), List.of(), List.of(), "");
        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand), "");
        Message userMessage = new Message(userManager.getUser("User1"), "!dev1 CM1 YYY", new Date());
        userMessage.setTargetDevice(externalDevice);

        Exception exception = Assertions.assertThrows(Exception.class, () -> {
            List<DeviceCommand> deviceCommands = Interpreter.interpret(userMessage);
        });

        Assertions.assertEquals("Parameter 1 needs to be from list: " + possibleValues, exception.getMessage());
    }

    @Test
    public void interpretCommandHelpTest() {
        UserManager userManager = new UserManager(List.of(), List.of());

        DeviceCommandParam param1 = new DeviceCommandParam("Param1", DeviceCommandParamType.String, List.of("ABC", "XXX"), 0, 0, false, "");
        DeviceCommand deviceCommand = new DeviceCommand("Command 1", "Command 1", "CM1", "cm_1", List.of(param1), List.of(), List.of(), "");
        ExternalDevice externalDevice = new ExternalDevice("dev1", null, List.of(), List.of(deviceCommand), "");
        Message userMessage = new Message(userManager.getUser("User1"), "!dev1 CM1 help", new Date());
        userMessage.setTargetDevice(externalDevice);

        Exception exception = Assertions.assertThrows(Exception.class, () -> Interpreter.interpret(userMessage));

        System.out.println(exception.getMessage());
//        Assertions.assertEquals("Z", exception.getMessage());
    }
}
