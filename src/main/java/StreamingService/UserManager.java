package StreamingService;

import Devices.Device;
import Devices.DeviceCommand;
import Devices.DeviceCommandParam;
import Devices.DeviceCommandParamType;
import Interpreter.Interpreter;
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
    private final List<String > bannedUserNames;
    private final Thread stopwatchThread;
    private final String systemName = "control";
    private final List<DeviceCommand> systemCommands = new ArrayList<>();

    public UserManager(List<String> bannedUserNames){
        this.bannedUserNames = bannedUserNames;
        activeUser = new SimpleObjectProperty<>();
        activeUser.set(null);

        activeUserTimerSeconds = new SimpleObjectProperty<>();
        activeUserTimerSeconds.set(0f);

        DeviceCommandParam requestCommandTimeParam = new DeviceCommandParam(
                "Request Time",
                DeviceCommandParamType.Integer,
                List.of(),
                3,
                10,
                false,
                "10"
        );

        DeviceCommand requestCommand = new DeviceCommand(
                "Request control",
                "Request control over devices by specifying number of minutes and enter the queue.",
                "request",
                "request",
                List.of(requestCommandTimeParam),
                List.of(),
                List.of(),
                ""
        );

        systemCommands.add(requestCommand);

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

    private void timeCounter(){
        //todo: thread counting down the activeUserTimerSeconds and frees the activeUser field

        // todo: if the userQueue is not empty, pop the user and start counting
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
            String command = message.getContent().split(" ")[1].replaceFirst("!", "");
            if(target.equals(systemName) && command.equals("request")){
                message.setTargetDevice(this);
                message.setType(MessageType.REQUEST_COMMAND);
            }
        }
    }

    @Override
    public void handleMessage(Message message) {
        logger.debug("Got message: " + message);

        if(message.getMessageType().equals(MessageType.REQUEST_COMMAND)){
            try {
                List<DeviceCommand> commandList = Interpreter.interpret(message);
                for(DeviceCommand command:commandList){
                    addCommandToExecute(command);
                }

            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

//        // todo: think about how to make interpreter interpret this command. Right now UserManager would have to be an Device impl but that would require too much unused methods to be in this class. Similarly, DeviceManager which also implements the Device interface only for its 4 commands to be interpreted by the Interpreter wouldn't have to be a device if Interpreter would work differently and recognize external devices and system components
//        if(message.getContent().startsWith("!request")){
//            String[] components = message.getContent().split(" ");
//            int duration = Integer.parseInt(components[1]);
//            if(duration <= 15 && duration > 0){
//                addRequest(message.getUser(), duration);
//            } else {
//                // todo: notify user about wrong parameters. Maybe throw a custom exception with Interpreter Message inside of it so when Chat Manager notifies all subscribers it can catch all messages about errors? And also then there would be less or no need for mediator
//                logger.error("Duration must be between 0 and 15 minutes");
//            }
//        }
    }

    @Override
    public String getName() {
        return "control";
    }

    @Override
    public void addCommandToExecute(DeviceCommand command) {
        String instruction = command.getDeviceInstructions().pop();

        if(instruction.split(" ")[0].equals("request")){
            int requestTime = Integer.parseInt(instruction.split(" ")[1]);
            String requestOwner = command.getOwner().getName();
            logger.debug("New request for user " + requestOwner + " : " + requestTime + "min");

            if(bannedUserNames.contains(requestOwner)){
                // todo: notify that user is banned or not?
                return;
            }

            addRequest(command.getOwner(), requestTime);
        }

        // todo: create new request and add it to the queue

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
