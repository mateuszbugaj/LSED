package Interpreter;


/*
The purpose of this class is to take UserMessage and generate based on its content DeviceCommands that can be
sent and understood by the device.
For example:
UserMessage        DeviceCommand
moveBy 10    -->  mv 10 0 0
moveBy 0 23  -->  mv 0 23 0
light on      -->  lgt on

Combined commands sent by the user in one message should also be possible to interpret:
UserMessage        DeviceCommand
moveBy 10;        mv 10 0 0
moveBy 0 24; -->  mv 0 23 0
light on;          lgt on

Interpreter should also be able to generate simple scenarios based on the single UserMessage:
UserMessage        DeviceCommand
                   mvto 230 50 10
Take 1        -->  grp 70
                   mvto 250 50 20

DeviceCommand class should be generated based on the .yaml config file for the device by some kind of creational design
pattern. It should be done by Interpreter.
List of DeviceCommands should be stored with the device but if that's possible Interpreter should be decoupled from
as many components as possible including Device.

Examples of different DeviceCommands in .yaml file.
1.
- moveBy
    description: "Move device by distance of millimeters."
    prefix: "moveBy"
    params:
        - XAxisDistance
            Type: Integer
            Range: [-100, 100]
        - YAxisDistance
            Type: Integer
            Range: [-100, 100]
            Optional: true
        - ZAxisDistance
            Type: Integer
            Range: [-100, 100]
            Optional: true
    devicePrefix: "mv"
- moveTo
    description: "Move device to absolute position specified in millimeters."
    prefix: "moveTo"
    params:
        - XAxisDistance
            Type: Integer
            Range: [-100, 100]
        - YAxisDistance
            Type: Integer
            Range: [-100, 100]
        - ZAxisDistance
            Type: Integer
            Range: [-100, 100]
    devicePrefix: "mvto"
- lightSwitch
    description: "Turn bed light ON or OFF. Specify intensity"
    prefix: "light"
    params:
        - state
            Type: String
            values: ['on', 'off']
        - intensity
            Type: Integer
            Range: [100]
            Optional: true
            Default: 80
    devicePrefix: "lgt"
 */

import Device.DeviceCommand;
import Device.DeviceCommandParam;
import Device.DeviceCommandParamType;
import StreamingService.UserMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);
    public static final String COMMAND_PREFIX = "!"; // todo: this maybe should be used to check the message for commands before these are sent to the interpreter
    public static final String COMMAND_SPLITTER = ";";

    public static DeviceCommand buildCommand(Map<String, Object> commandNode){
        DeviceCommand deviceCommand = new DeviceCommand();

        Object nameObject = commandNode.get("name");
        if(nameObject != null){
            deviceCommand.setName((String) nameObject);
            logger.info("Building command " + deviceCommand.getName());
        } else {
            throw new RuntimeException("Name is required");
        }

        Object descriptionObject = commandNode.get("description");
        if(descriptionObject != null){
            deviceCommand.setDescription((String) descriptionObject);
        }

        Object prefixObject = commandNode.get("prefix");
        if(prefixObject != null){
            deviceCommand.setPrefix((String) prefixObject);
        } else {
            throw new RuntimeException("Prefix is required");
        }

        Object devicePrefixObject = commandNode.get("devicePrefix");
        if(devicePrefixObject != null){
            deviceCommand.setDevicePrefix((String) devicePrefixObject);
        } else {
            throw new RuntimeException("DevicePrefix is required");
        }

        Object commandsObject = commandNode.get("params");
        if(commandsObject != null){
            for(Map<String, Object> param: (ArrayList<Map<String, Object>>) commandsObject){
                //todo: check if the optional parameters are last in the list of parameters
                deviceCommand.getParams().add(buildParameter(param));
            }
        }

        logger.debug(deviceCommand.toString());

        return deviceCommand;
    }

    private static DeviceCommandParam buildParameter(Map<String, Object> paramNode){
        DeviceCommandParam param = new DeviceCommandParam();

        Object nameObject = paramNode.get("name");
        if(nameObject != null){
            param.setName((String) nameObject);
        } else {
            throw new RuntimeException("Name is required");
        }

        Object typeObject = paramNode.get("type");
        if(typeObject != null){
            param.setType(DeviceCommandParamType.valueOf((String) typeObject));
        } else {
            throw new RuntimeException("Type is required");
        }

        Object rangeObject = paramNode.get("range");
        if(rangeObject != null){
            if(param.getType() != DeviceCommandParamType.Integer){
                throw new RuntimeException("Range can only be applied to parameter of Integer type.");
            }

            ArrayList<String> range = (ArrayList<String>) rangeObject;
            if(range.size() == 1){
//                param.rangeMin = 0; //todo: should this be equal to 0?
                param.setRangeMax(Integer.valueOf(String.valueOf(range.get(0))));
            } else if(range.size() == 2){
                param.setRangeMin(Integer.valueOf(String.valueOf(range.get(0))));
                param.setRangeMax(Integer.valueOf(String.valueOf(range.get(1))));
            } else {
                throw new RuntimeException("Range must have one or two values.");
            }
        }

        Object optionalObject = paramNode.get("optional");
        if(optionalObject != null){
            param.setOptional((Boolean) optionalObject);
        }

        Object defaultObject = paramNode.get("default");
        if(defaultObject != null){
            param.setDefaultValue(String.valueOf(defaultObject));
        }

        Object possibleValuesObject = paramNode.get("values");
        if(possibleValuesObject != null){
            ArrayList<String> possibleValues = (ArrayList<String>) possibleValuesObject;
            possibleValues.forEach(param::addPossibleValue);
        }

        return param;
    }

    public static ArrayList<String> interpret(UserMessage userMessage) throws Throwable {
        logger.debug("Interpreting UserMessage: " + userMessage.getContent());
        ArrayList<String> deviceInstructions = new ArrayList<>();

        if(!isCommand(userMessage)){
            logger.debug("Not a command");
            return deviceInstructions;
        }

        String commandContent = userMessage.getContent().substring(1); // Remove COMMAND_PREFIX
        List<String> commands = Arrays.stream(commandContent.split(COMMAND_SPLITTER)).map(String::strip).toList();
        logger.debug("UserMessage split into " + commands.size() + (commands.size() < 2 ? " command: " : " commands: ") + commands);
        for(String singularCommand:commands){
            String newInstruction = "";
            String[] commandComponents = singularCommand.split(" ");
            logger.debug("Singular command split into " + commandComponents.length + (commandComponents.length < 2 ? " component: " : " components: ") + Arrays.toString(commandComponents));
            if(userMessage.getTargetDevice() != null){
                int userCommandParametersNumber = commandComponents.length - 1;
                DeviceCommand deviceCommand = userMessage
                        .getTargetDevice()
                        .getDeviceCommands()
                        .stream()
                        .filter(i -> i.getPrefix().compareTo(commandComponents[0]) == 0)
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
                    deviceInstructions.addAll(deviceCommand.getEvents());
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
                    } else if(deviceCommand.getParams().get(i).getDefaultValue() != null){
                        logger.debug("Using default value for parameter " + i);
                        newInstruction = newInstruction.concat(" " + deviceCommand.getParams().get(i).getDefaultValue());
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
