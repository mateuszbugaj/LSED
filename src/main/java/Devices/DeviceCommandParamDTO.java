package Devices;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DeviceCommandParamDTO implements Comparable<DeviceCommandParamDTO>{
    private String name;
    private DeviceCommandParamType type;
    private List<Integer> range = new ArrayList<>();
    private Boolean optional = false;
    private String predefined;
    private List<String> values = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DeviceCommandParamType getType() {
        return type;
    }

    public void setType(DeviceCommandParamType type) {
        this.type = type;
    }

    public List<Integer> getRange() {
        return range;
    }

    public void setRange(List<Integer> range) {
        this.range = range;
    }

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public String getPredefined() {
        return predefined;
    }

    public void setPredefined(String predefined) {
        this.predefined = predefined;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    @Override
    public int compareTo(@NotNull DeviceCommandParamDTO o) {
        if(name.equals(o.getName()) &&
                type.equals(o.getType()) &&
                optional.equals(o.getOptional()) &&
//                (predefined != null && predefined.equals(o.getPredefined())) && // todo: find a way to compare null predefined
                Objects.equals(range, o.getRange())){
            return 0;
        }

        return -1;
    }

    @Override
    public String toString() {
        return "DeviceCommandParamDTO{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", range=" + range +
                ", optional=" + optional +
                ", predefined=" + predefined +
                ", values=" + values +
                '}';
    }
}
