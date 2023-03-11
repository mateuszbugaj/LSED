package Interpreter;

import Devices.*;
import StreamingService.Message;
import StreamingService.MessageType;
import Utils.ReturnMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);
    public static final String COMMAND_PREFIX = "!";
    public static final String COMMAND_SPLITTER = ";";

    public static DeviceCommand buildCommand(DeviceCommandDTO commandDTO){
        logger.debug("Building command " + commandDTO.getName());
        // todo: check if command is valid

        ArrayList<DeviceCommandParam> params = new ArrayList<>();
        for(DeviceCommandParamDTO paramDTO : commandDTO.getParams()){
            logger.debug("Building param " + paramDTO.getName());
            // todo: check if parameter is valid
            Integer maxValue = Integer.MAX_VALUE;
            Integer minValue = Integer.MIN_VALUE;
            if(paramDTO.getRange().size() == 1){
                maxValue = paramDTO.getRange().get(0);
            } else if(paramDTO.getRange().size() == 2){
                minValue = paramDTO.getRange().get(0);
                maxValue = paramDTO.getRange().get(1);
            } else if(paramDTO.getRange().size() > 2){
                logger.error("Too much range arguments");
            }

            DeviceCommandParam deviceCommandParam = new DeviceCommandParam(paramDTO.getName(), paramDTO.getType(), paramDTO.getValues(), minValue, maxValue, paramDTO.getOptional(), paramDTO.getPredefined());
            // todo: check if parameter is a duplicate (the same name and type),
            params.add(deviceCommandParam);
        }

        return new DeviceCommand(commandDTO.getName(), commandDTO.getDescription(), commandDTO.getPrefix(), commandDTO.getDevicePrefix(), params, commandDTO.getEvents(), commandDTO.getRequiredStates(), commandDTO.getResultingState());
    }

    public static List<DeviceCommand> interpret(Message message) throws ReturnMessageException {
        List<DeviceCommand> commandsToExecute = new ArrayList<>();
        if(!isCommand(message)){
            return commandsToExecute;
        }
        logger.debug("Interpreting command message: " + message);

        String commandContent = message.getContent().substring(1).substring(message.getContent().indexOf(' '));
        List<String> commands = Arrays.stream(commandContent.split(COMMAND_SPLITTER)).map(String::strip).toList();
        logger.debug("UserMessage split into " + commands.size() + (commands.size() < 2 ? " command: " : " commands: ") + commands);
        for(String command:commands){
            String[] commandComponents = command.split(" ");
            logger.debug("Singular command split into " +
                    commandComponents.length +
                    (commandComponents.length < 2 ? " component: " : " components: ") +
                    Arrays.toString(commandComponents));

            Device targetDevice = message.getTargetDevice();
            if(targetDevice == null){
                throw new ReturnMessageException("No device");
            }

            List<DeviceCommand> deviceCommandList = checkForMatchingDeviceCommands(commandComponents, targetDevice);
            logger.debug("Found " + deviceCommandList.size() + " commands with matching signature: \n" + deviceCommandList.stream().map(i -> i.toString() + "\n").collect(Collectors.joining()));
            if(deviceCommandList.isEmpty()){
                throw new ReturnMessageException("No matching command found for the device.");
            }

            for(DeviceCommand deviceCommand:deviceCommandList){
                if(!checkForDeviceCommandCorrectness(commandComponents, deviceCommand, targetDevice)){
                    continue;
                }

                logger.debug("Command '" + deviceCommand.getName() + "' is correct: " + deviceCommand);
                deviceCommand.setOwner(message.getUser());

                List<String> instructions = getInstructions(commandComponents, deviceCommand);
                logger.debug("Generated instructions: " + instructions);
                deviceCommand.setDeviceInstructions(instructions);
                commandsToExecute.add(deviceCommand);
                break;
            }

            if(commandsToExecute.isEmpty()){
                throw new ReturnMessageException("Command not correct.");
            }
        }

        return commandsToExecute;
    }

    private static List<DeviceCommand> checkForMatchingDeviceCommands(String[] commandComponents, Device targetDevice){
        String commandPrefix = commandComponents[0];
        int parametersNumber = commandComponents.length - 1;

        List<DeviceCommand> potentialDeviceCommands = targetDevice
                .getCommands()
                .stream()
                .filter(i -> i.getPrefix().compareTo(commandPrefix) == 0)
                .filter(i -> i.getParams().stream().filter(k -> !k.getOptional()).count() <= parametersNumber).toList();

        List<DeviceCommand> deviceCommands = new ArrayList<>();
        for(DeviceCommand deviceCommand:potentialDeviceCommands){
            boolean correctSignature = true;
            for(int paramId = 0; paramId<deviceCommand.getParams().size(); paramId++){
                if(paramId > (parametersNumber - 1)){
                    if(deviceCommand.getParams().get(paramId).getOptional()){
                    } else {
                        correctSignature = false;
                        break;
                    }
                } else {
                    if(deviceCommand.getParams().get(paramId).getType() == DeviceCommandParamType.Integer &&
                            !Pattern.compile("-?\\d+(\\.\\d+)?").matcher(commandComponents[paramId+1]).matches()){
                        correctSignature = false;
                    }
                }

            }

            if(correctSignature){
                deviceCommands.add(deviceCommand);
            }
        }

        /* Segregate deviceCommands by parameter types (integers go first) */
        List<DeviceCommand> segregatedDeviceCommands = deviceCommands.stream().sorted((o1, o2) -> {

            int equalElements = 0;
            for (int i = 0; i < o1.getParams().size(); i++) {

                if (o1.getParams().get(i).getType() == DeviceCommandParamType.Integer && o2.getParams().get(i).getType() == DeviceCommandParamType.String) {
                    return -1;
                }

                if (o1.getParams().get(i).getType() == o2.getParams().get(i).getType()) {
                    equalElements++;
                }
            }

            if (equalElements == o1.getParams().size()) {
                return 0;
            }

            return 1;
        }).toList();

        return segregatedDeviceCommands;
    }

    private static boolean checkForDeviceCommandCorrectness(String[] commandComponents, DeviceCommand deviceCommand, Device targetDevice) throws ReturnMessageException {
        String targetDeviceCurrentState = targetDevice.getCurrentState();
        if(targetDeviceCurrentState != null && !targetDeviceCurrentState.isBlank() && !deviceCommand.getRequiredStates().isEmpty() && deviceCommand.getRequiredStates().stream().noneMatch(i -> i.compareTo(targetDeviceCurrentState) == 0)){
            throw new ReturnMessageException("Device needs to be in the state: " + deviceCommand.getRequiredStates());
        }

        List<DeviceCommandParam> params = deviceCommand.getParams();
        int userCommandParametersNumber = commandComponents.length-1;
        for(int paramId = 0; paramId<params.size(); paramId++){
            DeviceCommandParam param = params.get(paramId);
            if(paramId < userCommandParametersNumber){
                String commandComponent = commandComponents[paramId + 1];
                if(param.getType() == DeviceCommandParamType.Integer){
                    int value = Integer.parseInt(commandComponent);
                    if(!(value >= param.getRangeMin() && value <= param.getRangeMax())){
                        return false;
                    }
                }
            } else {
                if(!params.get(paramId).getOptional()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static List<String> getInstructions(String[] commandComponents, DeviceCommand deviceCommand){
        if(deviceCommand.getEvents() == null || deviceCommand.getEvents().isEmpty()){
            String instruction = deviceCommand.getDevicePrefix();
            for(int paramId = 0; paramId < deviceCommand.getParams().size(); paramId++){
                DeviceCommandParam deviceCommandParam = deviceCommand.getParams().get(paramId);
                if(paramId >= (commandComponents.length - 1)){
                    instruction = instruction.concat(" ").concat(deviceCommandParam.getPredefined());
                } else {
                    String commandComponent = commandComponents[paramId + 1];
                    if(deviceCommandParam.getPossibleValues().isEmpty() || deviceCommandParam.getPossibleValues().stream().anyMatch(j -> j.equals(commandComponent))){
                        instruction = instruction.concat(" ").concat(commandComponent);
                    } else {
                        throw new RuntimeException("Parameter " + (paramId+1) + " needs to be from list: " + deviceCommandParam.getPossibleValues());
                    }
                }
            }
            return List.of(instruction);
        } else {
            List<String> events = deviceCommand.getEvents();
            // Collections.reverse(events); // todo: Why reverse?

            return events;
        }
    }

    public static boolean isCommand(Message message){
        return message.getContent().startsWith(COMMAND_PREFIX);
    }

}
