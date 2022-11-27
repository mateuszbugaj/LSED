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

public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);
    public static final String COMMAND_PREFIX = "/";
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

        return new DeviceCommand(commandDTO.getName(), commandDTO.getDescription(), commandDTO.getPrefix(), commandDTO.getDevicePrefix(), params, commandDTO.getEvents());
    }

    // todo: Maybe UserMessage should not have a type but it should by a inheritance of all these different types and then Interpreter could take only sub-type of UseCommand
    public static ArrayList<String> interpret(UserMessage userMessage) throws Throwable {
        logger.debug("Interpreting UserMessage: " + userMessage);
        ArrayList<String> deviceInstructions = new ArrayList<>();

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
            if(userMessage.getTargetDevice() != null){
                int userCommandParametersNumber = commandComponents.length - 1;
                String commandPrefix = commandComponents[0];
                DeviceCommand deviceCommand = userMessage
                        .getTargetDevice()
                        .getCommands()
                        .stream()
                        .filter(i -> i.getPrefix().compareTo(commandPrefix) == 0)
                        .filter(i -> i.getParams()
                                .stream()
                                .filter(k -> !k.getOptional()).count() <= userCommandParametersNumber)
                        .findAny()
                        .orElseThrow(new Supplier<Throwable>() { // todo: think how the interpreter should react on errors
                            @Override
                            public Throwable get() {
                                // todo: include information about not matching prefix or incorrect number of required parameters
                                logger.error("No matching commands found in the associated device");
                                return new RuntimeException("No matching commands found in the associated device");
                            }
                        });

                // todo: maybe events also could be made modifiable with parameters
                if(!deviceCommand.getEvents().isEmpty()){
                    logger.debug("Using events associated with the commands");
                    List<String> events = deviceCommand.getEvents();
                    Collections.reverse(events);
                    deviceInstructions.addAll(events);
                    return deviceInstructions;
                }

                newInstruction = deviceCommand.getDevicePrefix();
                for(int i = 0; i< deviceCommand.getParams().size(); i++){
                    if(i < userCommandParametersNumber){
                        String commandComponent = commandComponents[1 + i];
                        DeviceCommandParam param = deviceCommand.getParams().get(i);
                        if(param.getPossibleValues().isEmpty()){
                            if(param.getType() == DeviceCommandParamType.Integer){
                                Integer value = Integer.valueOf(commandComponent);
                                if(value >= param.getRangeMin() && value <= param.getRangeMax()){
                                    newInstruction = newInstruction.concat(" " + value);
                                } else {
                                    // throw an error about invalid range
                                }
                            }

                            if(param.getType() == DeviceCommandParamType.String){
                                newInstruction = newInstruction.concat(" " + commandComponent);
                            }
                        } else {
                            param.getPossibleValues().stream().filter(j -> j.compareTo(commandComponent) == 0).findAny().orElseThrow(new Supplier<Throwable>() {
                                @Override
                                public Throwable get() {
                                    logger.error("Parameter '" + commandComponent + "' needs to be from the list " + param.getPossibleValues());
                                    return new RuntimeException("Parameter '" + commandComponent + "' needs to be from the list " + param.getPossibleValues());
                                }
                            });

                            newInstruction = newInstruction.concat(" " + commandComponent);
                        }
                    } else if(deviceCommand.getParams().get(i).getPredefined() != null){
                        logger.debug("Using default value for parameter " + i);
                        newInstruction = newInstruction.concat(" " + deviceCommand.getParams().get(i).getPredefined());
                    }
                }

            } else {
                logger.debug("No device associated with the message");
            }

            deviceInstructions.add(newInstruction);
        }

        return deviceInstructions;
    }

    public static boolean isCommand(UserMessage userMessage){
        return userMessage.getContent().startsWith(COMMAND_PREFIX);
    }

}
