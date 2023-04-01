package Utils;

import java.util.List;

public class LSEDConfig {
    private static LSEDConfigDto dto;
    private static LSEDConfig instance;
    private LogRegister logRegister;

    private LSEDConfig(){
    }

    public static void load(LSEDConfigDto lsedConfig){
        dto = lsedConfig;
    }

    public static LSEDConfig get(){
        if(instance == null){
            instance = new LSEDConfig();
        }

        return instance;
    }

    public List<String> getDeviceConfigDir() {
        return dto.getDeviceConfigDir();
    }

    public List<String> getStreamConfigDir() {
        return dto.getStreamConfigDir();
    }

    public String getUserDatabaseDir() {
        if(dto.getUserDatabaseDir() != null){
            return dto.getUserDatabaseDir();
        }

        /* User database json file default path */
        return "src/main/resources/userDatabase.json";
    }

    public List<String> getAdminUsers(){
        return dto.getAdminUsers();
    }

    public String getChatLogDir() {
        return dto.getChatLogDir();
    }

    public LogRegister getLogRegister() {
        if (logRegister == null) {
            logRegister = new LogRegister(getChatLogDir());
        }

        return logRegister;
    }
}
