package Devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

// todo: this class should be immutable
public class DeviceCommand{
    private final String name;
    private final String description;
    private final String prefix;
    private final String devicePrefix;
    private final List<DeviceCommandParam> params;
    private final List<String> events; // todo: make it part of the distinct class DeviceCommandScenario
    private final List<String> requiredStates;
    private final String resultingState;
    private Stack<String> deviceInstructions;

    public DeviceCommand(String name, String description, String prefix, String devicePrefix, List<DeviceCommandParam> params, List<String> events, List<String> requiredStates, String resultingState) {
        this.name = name;
        this.description = description;
        this.prefix = prefix;
        this.devicePrefix = devicePrefix;
        this.params = params;
        this.events = events;
        this.requiredStates = requiredStates;
        this.resultingState = resultingState;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getDevicePrefix() {
        return devicePrefix;
    }

    public List<DeviceCommandParam> getParams() {
        return params;
    }

    public List<String> getEvents() {
        return new ArrayList<>(events);
    }

    public List<String> getRequiredStates() {
        return requiredStates;
    }

    public String getResultingState() {
        return resultingState;
    }

    public Stack<String> getDeviceInstructions() {
        return deviceInstructions;
    }

    public void setDeviceInstructions(List<String> deviceInstructions) {
        Stack<String> instructionStack = new Stack<>();
        instructionStack.addAll(deviceInstructions);
        this.deviceInstructions = instructionStack;
    }

    @Override
    public String toString() {
        //todo: modify it so that it can be displayed as manual for the command for the users
        return "DeviceCommand{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", prefix='" + prefix + '\'' +
                ", devicePrefix='" + devicePrefix + '\'' +
                ", params=" + params +
                ", events=" + events +
                '}';
    }
}
