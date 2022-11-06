package Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// todo: this class should be immutable
public class DeviceCommand{
    String name;
    String description;
    String prefix;
    String devicePrefix;
    ArrayList<DeviceCommandParam> params = new ArrayList<>();
    List<String> events = new ArrayList<>(); // todo: make it part of the distinct class DeviceCommandScenario

    public String getName() {
        return name;
    }

    public DeviceCommand setName(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DeviceCommand setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getPrefix() {
        return prefix;
    }

    public DeviceCommand setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public String getDevicePrefix() {
        return devicePrefix;
    }

    public DeviceCommand setDevicePrefix(String devicePrefix) {
        this.devicePrefix = devicePrefix;
        return this;
    }

    public ArrayList<DeviceCommandParam> getParams() {
        return params;
    }

    public List<String> getEvents() {
        return events;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceCommand that = (DeviceCommand) o;
        return name.equals(that.name) &&
                description.equals(that.description) &&
                prefix.equals(that.prefix) &&
                devicePrefix.equals(that.devicePrefix); // todo: add comparing params arrays
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, prefix, devicePrefix, params);
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
