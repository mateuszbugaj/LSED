package Interpreter;

import Devices.*;
import StreamingService.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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

    // todo: Maybe UserMessage should not have a type but it should by a inheritance of all these different types and then Interpreter could take only sub-type of UseCommand
    public static List<DeviceCommand> interpret(UserMessage userMessage) throws Throwable {
        logger.debug("Interpreting UserMessage: " + userMessage);
        List<DeviceCommand> commandsToExecute = new ArrayList<>();

//        if(!isCommand(userMessage)){
//            logger.debug("Not a command");
//            return deviceInstructions;
//        }

        String commandContent = userMessage.getContent().substring(1).substring(userMessage.getContent().indexOf(' ')); // Remove COMMAND_PREFIX todo: (like '!' or '/<device name>'), not sure yet
        List<String> commands = Arrays.stream(commandContent.split(COMMAND_SPLITTER)).map(String::strip).toList();
        logger.debug("UserMessage split into " + commands.size() + (commands.size() < 2 ? " command: " : " commands: ") + commands);
        for(String singularCommand:commands){
            String newInstruction = "";
            String[] commandComponents = singularCommand.split(" ");
            logger.debug("Singular command split into " + commandComponents.length + (commandComponents.length < 2 ? " component: " : " components: ") + Arrays.toString(commandComponents));
            Device targetDevice = userMessage.getTargetDevice();
            if(targetDevice != null){
                int userCommandParametersNumber = commandComponents.length - 1;
                String commandPrefix = commandComponents[0];

                List<DeviceCommand> deviceCommandList = targetDevice
                        .getCommands()
                        .stream()
                        .filter(i -> i.getPrefix().compareTo(commandPrefix) == 0)
                        .filter(i -> i.getParams().stream().filter(k -> !k.getOptional()).count() <= userCommandParametersNumber).toList();

                if(deviceCommandList.isEmpty()){ // todo: think how the interpreter should react on errors
                    throw new Throwable("No matching commands found in the associated device"); // todo: include information about not matching prefix or incorrect number of required parameters
                } else {
                    logger.debug("Found " + deviceCommandList.size() + " commands with matching signature");
                    for(DeviceCommand deviceCommand:deviceCommandList){
                        String targetDeviceCurrentState = targetDevice.getCurrentState();
                        if(targetDeviceCurrentState != null && !targetDeviceCurrentState.isBlank() && !deviceCommand.getRequiredStates().isEmpty()){
                            deviceCommand
                                    .getRequiredStates()
                                    .stream()
                                    .filter(state -> state.equals(targetDeviceCurrentState))
                                    .findAny()
                                    .orElseThrow(new Supplier<Throwable>() {
                                        @Override
                                        public Throwable get() {
                                            return new RuntimeException(
                                                    "Device must be in the one of the required states for this command: "
                                                            + deviceCommand.getRequiredStates()
                                                            + ". Current state: "
                                                            + targetDeviceCurrentState
                                                            + "."
                                            );
                                        }
                                    });
                        }

                        // todo: maybe events also could be made modifiable with parameters
                        if(!deviceCommand.getEvents().isEmpty()){
                            logger.debug("Using events associated with the commands");
                            List<String> events = deviceCommand.getEvents();

                            Collections.reverse(events);
                            deviceCommand.setDeviceInstructions(events);
                            commandsToExecute.add(deviceCommand);
                            continue;
                        }

                        newInstruction = deviceCommand.getDevicePrefix();
                        for(int i = 0; i< deviceCommand.getParams().size(); i++){
                            if(i < userCommandParametersNumber){
                                String commandComponent = commandComponents[1 + i];
                                DeviceCommandParam param = deviceCommand.getParams().get(i);
                                if(param.getPossibleValues().isEmpty()){
                                    // Check if the parameter is a number
                                    if(Pattern.compile("-?\\d+(\\.\\d+)?").matcher(commandComponent).matches()){
                                        if(param.getType() == DeviceCommandParamType.Integer){
                                            int value = Integer.parseInt(commandComponent);
                                            if(value >= param.getRangeMin() && value <= param.getRangeMax()){
                                                newInstruction = newInstruction.concat(" " + value);
                                            } else {
                                                // throw an error about invalid range
                                            }
                                        }
                                    } else {
                                        if(param.getType() == DeviceCommandParamType.String){
                                            newInstruction = newInstruction.concat(" " + commandComponent);
                                        }
                                    }

                                } else {
                                    param.getPossibleValues().stream().filter(j -> j.compareTo(commandComponent) == 0).findAny().orElseThrow(new Supplier<Throwable>() {
                                        @Override
                                        public Throwable get() {
                                            logger.error("Parameter '" + commandComponent + "' needs to be from the list " + param.getPossibleValues());
                                            return new RuntimeException("Parameter '" + commandComponent + "' needs to be from the list " + param.getPossibleValues());
                                        }
                                    });
                                }
                            } else if(deviceCommand.getParams().get(i).getPredefined() != null){
                                logger.debug("Using default value for parameter " + i);
                                newInstruction = newInstruction.concat(" " + deviceCommand.getParams().get(i).getPredefined());
                            }
                        }

                        deviceCommand.setDeviceInstructions(List.of(newInstruction));
                        commandsToExecute.add(deviceCommand);
                    }
                }

            } else {
                logger.debug("No device associated with the message");
            }
        }
        return commandsToExecute;
    }

    public static boolean isCommand(UserMessage userMessage){
        return userMessage.getContent().startsWith(COMMAND_PREFIX);
    }

}
