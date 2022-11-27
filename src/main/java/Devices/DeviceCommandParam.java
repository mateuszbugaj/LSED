package Devices;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// todo: this class should be immutable
public class DeviceCommandParam implements Comparable<DeviceCommandParam>{
    private final String name;
    private final DeviceCommandParamType type;
    private final List<String> possibleValues;
    private final Integer rangeMin; // applies only for Integer type
    private final Integer rangeMax;
    private final Boolean optional;
    private final String predefined;

    public DeviceCommandParam(String name, DeviceCommandParamType type, List<String> possibleValues, Integer rangeMin, Integer rangeMax, Boolean optional, String predefined) {
        this.name = name;
        this.type = type;
        this.possibleValues = possibleValues;
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
        this.optional = optional;
        this.predefined = predefined;
    }

    public String getName() {
        return name;
    }

    public DeviceCommandParamType getType() {
        return type;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

    public DeviceCommandParam addPossibleValue(String value){
        this.possibleValues.add(value);
        return this;
    }

    public Integer getRangeMin() {
        return rangeMin;
    }

    public Integer getRangeMax() {
        return rangeMax;
    }

    public Boolean getOptional() {
        return optional;
    }

    public String getPredefined() {
        return predefined;
    }

    @Override
    public int compareTo(@NotNull DeviceCommandParam o) {
        if(name.equals(o.getName()) &&
                type.equals(o.getType())){
            return 0;
        }

        return -1;
    }

    @Override
    public String toString() {
        return "DeviceCommandParam{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", possibleValues=" + possibleValues +
                ", rangeMin=" + rangeMin +
                ", rangeMax=" + rangeMax +
                ", optional=" + optional +
                ", predefined='" + predefined + '\'' +
                '}';
    }
}
