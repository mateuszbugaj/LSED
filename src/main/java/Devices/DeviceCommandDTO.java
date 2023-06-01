package Devices;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DeviceCommandDTO implements Comparable<DeviceCommandDTO>{
    private String name;
    private String description = "";
    private String prefix;
    private String output = "";
    private List<DeviceCommandParamDTO> params = new ArrayList<>();
    private List<String> events = new ArrayList<>();
    private List<String> requiredStates = new ArrayList<>();
    private String resultingState = "";
    private Map<String, String> vars = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public List<DeviceCommandParamDTO> getParams() {
        return params;
    }

    public void setParams(List<DeviceCommandParamDTO> params) {
        this.params = params;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public List<String> getRequiredStates() {
        return requiredStates;
    }

    public void setRequiredStates(List<String> requiredStates) {
        this.requiredStates = requiredStates;
    }

    public String getResultingState() {
        return resultingState;
    }

    public void setResultingState(String resultingState) {
        this.resultingState = resultingState;
    }

    public Map<String, String> getVars() {
        return vars;
    }

    public void setVars(Map<String, String> vars) {
        this.vars = vars;
    }

    @Override
    public int compareTo(@NotNull DeviceCommandDTO o) {
        if(!(name.equals(o.getName()) &&
                description.equals(o.getDescription()) &&
                prefix.equals(o.getPrefix()) &&
                output.equals(o.getOutput()))){
            return -1;
        }

        if(params.size() != o.getParams().size()){
            return -1;
        }

        for(int i = 0; i < params.size(); i++){
            boolean isPresent = false;
            for(int j = 0; j < o.getParams().size(); j++){
                if(params.get(i).compareTo(o.getParams().get(j)) == 0){
                    isPresent = true;
                    break;
                }
            }

            if(!isPresent){
                return -1;
            }
        }

        // todo: compare events

        if(!resultingState.equals(o.getResultingState())) return -1;

        for(int i = 0; i < requiredStates.size(); i++){
            boolean isPresent = false;
            for(int j = 0; j < o.getRequiredStates().size(); j++){
                if(requiredStates.get(i).equals(o.getRequiredStates().get(j))){
                    isPresent = true;
                    break;
                }
            }

            if(!isPresent){
                return -1;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "DeviceCommandDTO{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", prefix='" + prefix + '\'' +
                ", devicePrefix='" + output + '\'' +
                ", params=" + params +
                '}';
    }
}
