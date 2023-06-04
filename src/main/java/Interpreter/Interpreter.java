package Interpreter;

import Devices.*;
import StreamingService.Message;
import StreamingService.MessageType;
import Utils.ReturnMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
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

        List<String> replacedVariablesString = new ArrayList<>();
        for(String event: commandDTO.getEvents()){
            replacedVariablesString.add(replaceVariables(event, commandDTO.getVars()));
        }

        return new DeviceCommand(commandDTO.getName(), commandDTO.getDescription(), commandDTO.getPrefix(), commandDTO.getOutput(), params, replacedVariablesString, commandDTO.getRequiredStates(), commandDTO.getResultingState());
    }

    public static String replaceVariables(String input, Map<String, String> variables) {
        String result = input;
        Pattern variablePattern = Pattern.compile("\\$\\w+");

        // Detect and report undefined variables
        Matcher matcher = variablePattern.matcher(input);
        while (matcher.find()) {
            String variable = matcher.group();
            String variableKey = variable.substring(1);
            if (!variables.containsKey(variableKey)) {
                logger.error("Undefined variable: " + variable);
            }
        }

        // Replace variables
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String variable = "$" + entry.getKey();
            String value = entry.getValue();
            result = result.replace(variable, value);
        }
        return result;
    }

    // todo: bug: when there is command with parameter in type of String and Integer, the range for Integer is not recognized and parameter is treated as string
    public static List<DeviceCommand> interpret(Message message) throws ReturnMessageException {
        List<DeviceCommand> commandsToExecute = new ArrayList<>();
        if(!isCommand(message)){
            return commandsToExecute;
        }
        logger.debug("Interpreting command message: " + message);

        String commandContent = message.getContent().substring(1).substring(message.getContent().indexOf(' '));
        List<String> commands = Arrays.stream(commandContent.split(COMMAND_SPLITTER)).map(String::strip).collect(Collectors.toList());
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

            if(commandComponents.length >= 2 && commandComponents[1].equals("help")){
                String commandsHelp = targetDevice.getCommands().stream().filter(i -> i.getPrefix().equals(commandComponents[0])).map(i -> i.getHelpMessage() + '\n').collect(Collectors.joining());
                throw new ReturnMessageException(commandsHelp, MessageType.INFO);
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
                .filter(i -> i.getParams().stream().filter(k -> !k.getOptional()).count() <= parametersNumber).collect(Collectors.toList());

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
        }).collect(Collectors.toList());

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
                    float value = Float.parseFloat(commandComponent);
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

    private static List<String> getInstructions(String[] commandComponents, DeviceCommand deviceCommand) throws ReturnMessageException {
        if(deviceCommand.getEvents() == null || deviceCommand.getEvents().isEmpty()){

            String instruction = deviceCommand.getOutput();

            // get parameters from the device command
            List<DeviceCommandParam> params = deviceCommand.getParams();

            // create a map from param name to command component
            Map<String, String> paramMap = new HashMap<>();
            for(int i = 0; i < params.size(); i++){
                System.out.println("> " + params.get(i).getName());
                if(commandComponents.length - 1 > i){
                    paramMap.put(params.get(i).getName(), commandComponents[i+1]); // +1 to ignore the prefix
                } else if(!params.get(i).getOptional()){
                    throw new ReturnMessageException("Parameter " + params.get(i).getName() + " is required.");
                } else if(params.get(i).getOptional()){
                    paramMap.put(params.get(i).getName(), "");
                }
            }

            // replace variables in the instruction with corresponding values
            for(Map.Entry<String, String> entry : paramMap.entrySet()){
                instruction = instruction.replace("$" + entry.getKey(), entry.getValue());
            }

            return List.of(instruction);
        } else {
            List<String> events = deviceCommand.getEvents();
            Collections.reverse(events); // todo: Why reverse?

            return events;
        }
    }

    public static boolean isCommand(Message message){
        return message.getContent().startsWith(COMMAND_PREFIX);
    }

}
