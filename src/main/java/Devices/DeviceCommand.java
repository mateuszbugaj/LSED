package Devices;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// todo: this class should be immutable
public class DeviceCommand{
    private final String name;
    private final String description;
    private final String prefix;
    private final String devicePrefix;
    private final List<DeviceCommandParam> params;
    private final List<String> events; // todo: make it part of the distinct class DeviceCommandScenario

    public DeviceCommand(String name, String description, String prefix, String devicePrefix, List<DeviceCommandParam> params, List<String> events) {
        this.name = name;
        this.description = description;
        this.prefix = prefix;
        this.devicePrefix = devicePrefix;
        this.params = params;
        this.events = events;
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
        return events;
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
