package Utils;

import java.util.List;

public class LSEDConfigDto {
    private List<String> deviceConfigDir;
    private List<String> streamConfigDir;
    private String userDatabaseDir;
    private List<String> adminUsers;
    private String chatLogDir;

    public List<String> getDeviceConfigDir() {
        return deviceConfigDir;
    }

    public void setDeviceConfigDir(List<String> deviceConfigDir) {
        this.deviceConfigDir = deviceConfigDir;
    }

    public List<String> getStreamConfigDir() {
        return streamConfigDir;
    }

    public void setStreamConfigDir(List<String> streamConfigDir) {
        this.streamConfigDir = streamConfigDir;
    }

    public String getUserDatabaseDir() {
        return userDatabaseDir;
    }

    public void setUserDatabaseDir(String userDatabaseDir) {
        this.userDatabaseDir = userDatabaseDir;
    }

    public List<String> getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(List<String> adminUsers) {
        this.adminUsers = adminUsers;
    }

    public String getChatLogDir() {
        return chatLogDir;
    }

    public void setChatLogDir(String chatLogDir) {
        this.chatLogDir = chatLogDir;
    }
}
