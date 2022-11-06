package Device;

import java.util.ArrayList;
import java.util.List;

// todo: this class should be immutable
public class DeviceCommandParam{
    String name;
    DeviceCommandParamType type;
    List<String> possibleValues = new ArrayList<>(); // can be empty todo: should be empty or null?
    Integer rangeMin = Integer.MIN_VALUE; // applies only for Integer type
    Integer rangeMax = Integer.MAX_VALUE;
    Boolean optional = false;
    String defaultValue; // can be empty // todo: implement

    public String getName() {
        return name;
    }

    public DeviceCommandParam setName(String name) {
        this.name = name;
        return this;
    }

    public DeviceCommandParamType getType() {
        return type;
    }

    public DeviceCommandParam setType(DeviceCommandParamType type) {
        this.type = type;
        return this;
    }

    public List<String> getPossibleValues() {
        return possibleValues;
    }

//    public DeviceCommandParam setPossibleValues(List<String> possibleValues) {
//        this.possibleValues = possibleValues;
//        return this;
//    }

    public DeviceCommandParam addPossibleValue(String value){
        this.possibleValues.add(value);
        return this;
    }

    public Integer getRangeMin() {
        return rangeMin;
    }

    public DeviceCommandParam setRangeMin(Integer rangeMin) {
        this.rangeMin = rangeMin;
        return this;
    }

    public Integer getRangeMax() {
        return rangeMax;
    }

    public DeviceCommandParam setRangeMax(Integer rangeMax) {
        this.rangeMax = rangeMax;
        return this;
    }

    public Boolean getOptional() {
        return optional;
    }

    public DeviceCommandParam setOptional(Boolean optional) {
        this.optional = optional;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public DeviceCommandParam setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }
}
