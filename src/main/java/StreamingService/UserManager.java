package StreamingService;

import Devices.Device;
import Devices.DeviceCommand;
import Devices.DeviceCommandParam;
import Devices.DeviceCommandParamType;
import Interpreter.Interpreter;
import Utils.ReturnMessageException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserManager implements MessageSubscriber, Device {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
    private final List<User> users = new ArrayList<>();
    private SimpleObjectProperty<User> activeUser;
    private SimpleObjectProperty<Float> activeUserTimerSeconds;
    private ObservableList<UserRequest> userQueue = FXCollections.observableArrayList();
    private final Thread stopwatchThread;
    private final String systemName = "control";
    private final List<DeviceCommand> systemCommands = new ArrayList<>();

    public UserManager(List<String> bannedUserNames, List<String> adminUserNames){
        for(String username:bannedUserNames){
            User user = new User(username);
            user.setBanned(true);
            users.add(user);
        }

        for(String username:adminUserNames){
            User user = new User(username);
            user.giveAdminPrivileges();
            users.add(user);
        }

        activeUser = new SimpleObjectProperty<>();
        activeUser.set(null);

        activeUserTimerSeconds = new SimpleObjectProperty<>();
        activeUserTimerSeconds.set(0f);

        DeviceCommand requestCommand = new DeviceCommand(
                "Request control",
                "Request control over devices by specifying number of minutes and enter the queue.",
                "request",
                "request",
                List.of(new DeviceCommandParam(
                        "Request Time",
                        DeviceCommandParamType.Integer,
                        List.of(),
                        3,
                        10, // todo: max request time should be read from the config file
                        false,
                        "10"),
                        new DeviceCommandParam(
                                "Username",
                                DeviceCommandParamType.String,
                                List.of(),
                                0,
                                0,
                                true,
                                "")
                ),
                List.of(),
                List.of(),
                ""
        );

        systemCommands.add(requestCommand);

        DeviceCommand banUserCommand = new DeviceCommand(
                "Ban User",
                "Ban user by username",
                "ban",
                "ban",
                List.of(new DeviceCommandParam(
                        "User name",
                        DeviceCommandParamType.String,
                        List.of(),
                        0,
                        0,
                        false,
                        "")
                ),
                List.of(),
                List.of(),
                ""
        );

        systemCommands.add(banUserCommand);

        DeviceCommand unbanUserCommand = new DeviceCommand(
                "Unban User",
                "Unban user by username",
                "unban",
                "unban",
                List.of(new DeviceCommandParam(
                        "User name",
                        DeviceCommandParamType.String,
                        List.of(),
                        0,
                        0,
                        false,
                        "")
                ),
                List.of(),
                List.of(),
                ""
        );

        systemCommands.add(unbanUserCommand);

        stopwatchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Stopwatch thread is running");

                while(true){
                    if(activeUserTimerSeconds.get() > 0){
                        activeUserTimerSeconds.set(activeUserTimerSeconds.get()-1);;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if(activeUserTimerSeconds.get() == 0) {
                        if(activeUser.get() != null){
                            logger.debug("User session for " + activeUser.get().getName() + " has expired");
                            activeUser.set(null);
                        }

                        if(userQueue.size() != 0){
                            UserRequest userRequest = userQueue.remove(0);
                            activeUser.set(userRequest.getUser());
                            activeUserTimerSeconds.set((float) userRequest.getTimeSeconds());
                        }
                    }
                }
            }
        });

        stopwatchThread.start();
    }

    public SimpleObjectProperty<User> getActiveUser(){
        return activeUser;
    }

    public ObservableList<UserRequest> getUserQueue() {
        return userQueue;
    }

    public SimpleObjectProperty<Float> getActiveUserTimerSeconds(){
        return activeUserTimerSeconds;
    }

    public User getUser(String name){
        Optional<User> optionalUser = users.stream().filter(user -> Objects.equals(user.getName(), name)).findAny();
        if(optionalUser.isPresent()){
            return optionalUser.get();
        }

        User user = new User(name);
        logger.debug("New user added: " + user.getName());
        users.add(user);
        return user;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addRequest(User user, float time){
        if(activeUser.get() == null){
            activeUser.set(user);
            activeUserTimerSeconds.set(time * 60f);
        } else {
            UserRequest userRequest = new UserRequest(user, (int) time * 60);
            userQueue.add(userRequest);
        }
    }

    @Override
    public void annotateMessage(Message message) {
        if(message.getMessageType().equals(MessageType.COMMAND)){
            String target = message.getContent().split(" ")[0].replaceFirst("!", "");
            if(target.equals(systemName)){
                message.setTargetDevice(this);
                message.setType(MessageType.CONTROL_COMMAND);
            }
        }
    }

    @Override
    public void handleMessage(Message message) throws ReturnMessageException {
        if(message.getMessageType().equals(MessageType.CONTROL_COMMAND)){
            List<DeviceCommand> commandList = Interpreter.interpret(message);
            for(DeviceCommand command:commandList){
                addCommandToExecute(command);
            }
        }

    }

    @Override
    public String getName() {
        return "control";
    }

    @Override
    public void addCommandToExecute(DeviceCommand command) throws ReturnMessageException {
        String instruction = command.getDeviceInstructions().pop();

        String commandPrefix = instruction.split(" ")[0];
        switch (commandPrefix){
            case "request" -> {
                int requestTime = Integer.parseInt(instruction.split(" ")[1]);
                String requestOwner;

                if(instruction.split(" ").length == 3){
                    if(command.getOwner().hasAdminPrivileges()){
                        requestOwner = instruction.split(" ")[2];
                    } else {
                        throw new ReturnMessageException("Needs admin privilege");
                    }
                } else {
                    requestOwner = command.getOwner().getName();
                }


                logger.debug("New request for user " + requestOwner + " : " + requestTime + "min");

                if(getUser(requestOwner).isBanned()){
                    throw new ReturnMessageException("User " + requestOwner + " is banned from controlling the machine!");
                }

                addRequest(getUser(requestOwner), requestTime);
            }
            case "ban" -> {
                String userName = instruction.split(" ")[1];
                User user = getUser(userName);
                user.setBanned(true);
                throw new ReturnMessageException("User " + userName + " is now banned.", MessageType.INFO);
            }

            case "unban" -> {
                String userName = instruction.split(" ")[1];
                User user = getUser(userName);
                user.setBanned(true);
                if(user.isBanned()){
                    user.setBanned(false);
                    throw new ReturnMessageException("User " + userName + " is now unbanned.", MessageType.INFO);
                } else {
                    throw new ReturnMessageException("User " + userName + " is not on banned list.", MessageType.ERROR);
                }
            }
        }
    }

    @Override
    public List<DeviceCommand> getCommands() {
        return systemCommands;
    }

    @Override
    public String getCurrentState() {
        return ""; // Not a state machine
    }
}
