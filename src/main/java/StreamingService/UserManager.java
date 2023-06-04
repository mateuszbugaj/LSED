package StreamingService;

import Devices.Device;
import Devices.DeviceCommand;
import Devices.DeviceCommandParam;
import Devices.DeviceCommandParamType;
import Interpreter.Interpreter;
import Utils.LSEDConfig;
import Utils.ReturnMessageException;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UserManager implements MessageSubscriber, Device {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);
    private SimpleObjectProperty<User> activeUser;
    private SimpleObjectProperty<Float> activeUserTimerSeconds;
    private ObservableList<UserRequest> userQueue = FXCollections.observableArrayList();
    private final Thread stopwatchThread;
    private final String systemName = "control";
    private final List<DeviceCommand> systemCommands = new ArrayList<>();
    private final UserDatabase userDatabase;

    public UserManager(List<String> adminUserNames){
        userDatabase = new UserDatabase(LSEDConfig.get().getUserDatabaseDir());

        /* Create the Admin user just right here */
        giveAdminPrivileges(getUser("Admin"));

        if(adminUserNames != null){
            for(String username:adminUserNames){
                User user = getUser(username);
                giveAdminPrivileges(user);
            }
        }

        activeUser = new SimpleObjectProperty<>();
        activeUser.set(null);

        activeUserTimerSeconds = new SimpleObjectProperty<>();
        activeUserTimerSeconds.set(0f);

        DeviceCommand requestCommand = new DeviceCommand(
                "Request control",
                "Request control over devices by specifying number of minutes and enter the queue.",
                "request",
                "request $request_time $username",
                List.of(new DeviceCommandParam(
                        "request_time",
                        DeviceCommandParamType.Integer,
                        List.of(),
                        3,
                        10, // todo: max request time should be read from the config file
                        false,
                        "10"),
                        new DeviceCommandParam(
                                "username",
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
                "ban $user_name",
                List.of(new DeviceCommandParam(
                        "user_name",
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
                "unban $user_name",
                List.of(new DeviceCommandParam(
                        "user_name",
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
        Optional<User> optionalUser = userDatabase.get().stream().filter(user -> Objects.equals(user.getName(), name)).findAny();
        if(optionalUser.isPresent()){
            return optionalUser.get();
        }

        User user = new User(name);
        logger.debug("New user added: " + user.getName());
        userDatabase.add(user);
        return user;
    }

    public List<User> getUsers() {
        return userDatabase.get();
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

    public void giveAdminPrivileges(User user){
        user.setAdmin(true);
        userDatabase.add(user);
    }

    public void banUser(User user){
        user.setBanned(true);
        userDatabase.add(user);

        userQueue.removeIf(usr -> usr.getUser().getName().equals(user.getName()));
        if(activeUser.get().getName().equals(user.getName())){
            activeUser.set(null);
            activeUserTimerSeconds.set(0f);
        }
    }

    public void unbanUser(User user){
        user.setBanned(false);
        userDatabase.add(user);
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
                    if(command.getOwner().isAdmin()){
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
                banUser(user);
                throw new ReturnMessageException("User " + userName + " is now banned.", MessageType.INFO);
            }

            case "unban" -> {
                String userName = instruction.split(" ")[1];
                User user = getUser(userName);
                if(user.isBanned()){
                    unbanUser(user);
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
