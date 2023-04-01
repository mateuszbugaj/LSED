package View;

import Devices.*;
import StreamingService.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// The view class
// Each component of the GUI should be its own object
public class MainWindow implements MessageSubscriber {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    private final Stage stage;
    private final DeviceManager deviceManager;
    private final ChatManager chatManager;
    private final ArrayList<Message> messages = new ArrayList<>();
    private final VBox messageCellList = new VBox();
    private final UserManager userManager;

    public MainWindow(Stage s, DeviceManager deviceManager, ChatManager chatManager, UserManager userManager){
        stage = s;
        this.deviceManager = deviceManager;
        this.chatManager = chatManager;
        this.userManager = userManager;

        stage.setWidth(1200);
        stage.setHeight(800);
        stage.setFullScreen(true);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setX(2000); // 100 (2000 for second display)
        stage.setY(100);
    }

    public void show(){
        showMainBorderPane(stage);

        stage.show();
    }

    /*
    +----------+--------------------+
    |          |                    |
    | UserBox  |       ViewBox      |
    |          |                    |
    |          |                    |
    |          |                    |
    +----------+--------------------+
     */
    private void showMainBorderPane(Stage stage){
        BorderPane mainBorderPane = new BorderPane();
        Scene scene = new Scene(mainBorderPane);
        stage.setScene(scene);

        mainBorderPane.setCenter(generateViewBox());
        mainBorderPane.setLeft(generateUserBox());
    }

    /*


    +-----------------------------+
    |          StatusBar          |
    +-----------------+-----------+
    |     ViewBox     |           |
    |                 | DeviceBox |
    |    MainFrame    |           |
    |                 |           |
    +-----------------+           |
    |   Thumbnails    |           |
    +-----------------+-----------+

     */
    private BorderPane generateViewBox(){
        BorderPane viewBox = new BorderPane();
        viewBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        viewBox.setPadding(new Insets(0, 0, 10, 0));

        ExternalDevice device;
        ArrayList<Camera> cameras = new ArrayList<>();
        Pane mainFramePane = new Pane();
        try{
            device = deviceManager.getDevice(deviceManager.selectedDeviceIndex.get());
            cameras.addAll(device.getCameras());
            mainFramePane.getChildren().add(cameras.get(0).getFrameView());
            cameras.get(0).getFrameView().fitWidthProperty().bind(mainFramePane.widthProperty());
        } catch (IndexOutOfBoundsException e){
            logger.error(e.getMessage());
        }

        BorderPane deviceBorderPane = new BorderPane();
        viewBox.setCenter(mainFramePane);
        viewBox.setBottom(generateThumbnails(cameras));

        deviceBorderPane.setCenter(viewBox);


        deviceManager.selectedDeviceIndex.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() == -1) return;
                ExternalDevice device = deviceManager.getDevice((int) newValue);
                Camera deviceSelectedCamera = device.getSelectedCamera();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        mainFramePane.getChildren().clear();
                        mainFramePane.getChildren().add(deviceSelectedCamera.getFrameView());
                        deviceSelectedCamera.getFrameView().fitWidthProperty().bind(mainFramePane.widthProperty());
                        viewBox.setCenter(mainFramePane);
                        viewBox.setBottom(generateThumbnails(device.getCameras()));

                        deviceBorderPane.setCenter(viewBox);
                    }
                });
            }
        });

        deviceBorderPane.setTop(generateStatusBar());
        deviceBorderPane.setRight(generateDeviceBox());

        return deviceBorderPane;
    }

    /*
               viewBoxHBox
    +--------------------------------+
    |+-----------+ +-----------+ +---|
    || thumbnail | | thumbnail | | th|
    ||           | |           | |   |
    |+-----------+ +-----------+ +---|
    +--------------------------------+
     */
    private HBox generateThumbnails(List<Camera> thumbnailCameraList){
        HBox viewBoxHBox = new HBox(10);
        viewBoxHBox.setMinHeight(200);
        viewBoxHBox.setPrefHeight(200);
        viewBoxHBox.setStyle("-fx-background-color: linear-gradient(to top, black, transparent);");
        viewBoxHBox.setPadding(new Insets(10));

        for(Camera camera: thumbnailCameraList){
            ImageView thumbnail = new ImageView();
            thumbnail.imageProperty().bind(camera.getFrameView().imageProperty());
            Pane thumbnailPane = new Pane(thumbnail);
            thumbnail.fitHeightProperty().bind(thumbnailPane.heightProperty());
            thumbnail.setPreserveRatio(true);

            Pane thumbnailGradient = new Pane();
            thumbnailGradient.setPrefWidth(240); // todo: it should be bind property to thumbnail width
            thumbnailGradient.setPrefHeight(100);
            thumbnailGradient.setStyle("-fx-background-color: linear-gradient(to bottom, black, transparent);");
            thumbnailPane.getChildren().add(thumbnailGradient);

            Text text = new Text((String) camera.getFrameView().getUserData());
            text.setFill(Color.WHITE);
            text.setStroke(Color.WHITE);
            text.setFont(new Font(10));

            // todo: set name text wrapping width as width of the thumbnail
//            text.wrappingWidthProperty().bind(thumbnailPane.widthProperty());
            text.setY(20);
            text.setX(10);
            thumbnailPane.getChildren().add(text);

            viewBoxHBox.getChildren().add(thumbnailPane);
        }

        return viewBoxHBox;
    }

    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }

    /*
                userBox
    /--------userBoxWidth---------/
    +-----------------------------+
    | +         titleBox        | |
    | |                         | |
    | +-------------------------+ |
    |          userQueue          |
    |                             |
    |                             |
    +-----------------------------+
    |                             |
    |                             |
    |                             |
    |                             |
    |                             |
    |                             |
    +-----------------------------+
     */
    private VBox generateUserBox(){
        VBox userBox = new VBox();
        int userBoxWidth = 300;
        userBox.setMinWidth(userBoxWidth);
        userBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        userBox.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 3, 0, 0))));
        userBox.setPadding(new Insets(10));
        userBox.setSpacing(10);

        /*
        Info about user in charge or current event ect.
        todo: Read this from the config file and add text wrapping

              titleBox
        +-----------------+
        |     titleText   |
        |   subTitleText  |
        +-----------------+
         */

        VBox titleVBox= new VBox();
        titleVBox.setSpacing(10);
        titleVBox.setPadding(new Insets(10));
        titleVBox.setBorder(new Border(new BorderStroke(
                Paint.valueOf("white"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1, 1, 1, 1)
        )));

        StackPane titleTextStackPane = new StackPane();
        Text title = new Text("LSED");
        title.setFont(new Font(25));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setFill(Paint.valueOf("white"));
        titleTextStackPane.getChildren().add(title);

        Text subTitle = new Text("Live Stream External Device");
        subTitle.setFont(new Font(15));
        subTitle.setFill(Paint.valueOf("white"));

        titleVBox.getChildren().addAll(titleTextStackPane, subTitle);
        userBox.getChildren().add(titleVBox);

        /*
        User in control and for how long

               userQueue
        +---------------------+
        |  userInControlUsername  |
        +---------------------+
        |   userQueueContent  |
        |                     |
        |    userControlTip   |
        +---------------------+

         */
        VBox userQueueVBox= new VBox();
        userQueueVBox.setSpacing(10);
        userQueueVBox.setPadding(new Insets(10, 0, 10, 0));
        userQueueVBox.setBorder(new Border(new BorderStroke(
                Paint.valueOf("white"),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 3, 0)
        )));

        /*
                                   userInControl
        +--------------------------------------------------------------+
        |                                                              |
        | userInControlUsername                     userInControlTimer |
        |                                                              |
        +--------------------------------------------------------------+
         */
        BorderPane userInControl = new BorderPane();

        Text userInControlUsername = new Text("No one in control");
        Text userInControlTimer = new Text();
        userManager.getActiveUser().addListener(new ChangeListener<User>() {
            @Override
            public void changed(ObservableValue<? extends User> observableValue, User user, User newUser) {
                if(newUser != null){
                    userInControlUsername.setText(newUser.getName());
                } else {
                    userInControlUsername.setText("No one in control");
                    userInControlTimer.setText("");
                }
            }
        });
        userInControlUsername.setFont(new Font(25));
        userInControlUsername.setFill(Paint.valueOf("green"));

        userManager.getActiveUserTimerSeconds().addListener(new ChangeListener<Float>() {
            @Override
            public void changed(ObservableValue<? extends Float> observable, Float oldValue, Float newValue) {
                userInControlTimer.setText(formatTime(newValue.intValue()));
            }
        });
        userInControlTimer.setFont(new Font("Monospaced Bold", 25));
        userInControlTimer.setFill(Paint.valueOf("green"));

        userInControl.setLeft(userInControlUsername);
        userInControl.setRight(userInControlTimer);
        userQueueVBox.getChildren().add(userInControl);

        /*
                                  userQueueContent
        +--------------------------------------------------------------+
        | userControlQueueUsername                userControlQueueTime |
        | User1                                                      5 |
        | User2                                                      8 |
        | User1                                                     15 |
        | And 8 more...                                                |
        +--------------------------------------------------------------+
         */
        VBox userQueueContent = new VBox();
        userQueueContent.getChildren().add(new Text(""));

        userManager.getUserQueue().addListener(new ListChangeListener<UserRequest>() {
            @Override
            public void onChanged(Change<? extends UserRequest> c) {
                Platform.runLater(() -> {
                    userQueueContent.getChildren().clear();
                    int counter = 0;
                    for(UserRequest userRequest: userManager.getUserQueue()){

                        BorderPane row = new BorderPane();
                        Text requestUsername = new Text(userRequest.getUser().getName());
                        requestUsername.setFont(new Font(15));
                        requestUsername.setFill(Paint.valueOf("white"));
                        Text requestTime = new Text(formatTime(userRequest.getTimeSeconds()));
                        requestTime.setFont(new Font(15));
                        requestTime.setFill(Paint.valueOf("white"));

                        row.setLeft(requestUsername);
                        row.setRight(requestTime);

                        userQueueContent.getChildren().addAll(row);

                        if(counter++ > 5){
                            Text queueInfo = new Text("And " + (userManager.getUserQueue().size() - 5) + " more...");
                            queueInfo.setWrappingWidth(userBoxWidth);
                            queueInfo.setFont(new Font(15));
                            queueInfo.setFill(Paint.valueOf("white"));
                            userQueueContent.getChildren().add(queueInfo);
                            break;
                        }
                    }
                });
            }
        });

        userQueueVBox.getChildren().add(userQueueContent);

        Text userControlTip = new Text("Type: \n'!request <time in minutes (max 30)>'"); // todo: take max from the config file
        userControlTip.setWrappingWidth(userBoxWidth);
        userControlTip.setFont(new Font(12));
        userControlTip.setFill(Paint.valueOf("grey"));
        userQueueVBox.getChildren().add(userControlTip);

        userBox.getChildren().addAll(userQueueVBox);

        messageCellList.setSpacing(10);
        messageCellList.setPrefHeight(2000);
        messageCellList.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));

        ScrollPane scrollPane = new ScrollPane(messageCellList);
        scrollPane.setPrefHeight(2000);
        scrollPane.setRotate(180);
        scrollPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        userBox.getChildren().add(scrollPane);

        return userBox;
    }

    private TextFlow generateUserMessageCell(Message message){
        Text date;
        Text user;
        Text device;
        Text content;

        date = new Text("[" + new SimpleDateFormat("HH:mm:ss").format(message.getTimestamp()) + "] ");
        date.setFill(Color.LIGHTSLATEGREY);

        user = new Text(message.getUser().getName() + ": ");
        user.setFill(Color.GREY);

        if(message.getTargetDevice() == null){
            device = new Text(" ");

        } else {
            device = new Text("/" + message.getTargetDevice().getName() + " ");
        }
        device.setFill(Color.GREY);

        content = new Text(message.getContent());
        content.setFill(Color.GREY);

        switch (message.getMessageOwnership()){
            case USER -> {
                user.setFill(Color.MEDIUMSEAGREEN);
            }

            case ADMIN -> {
                user.setFill(Color.RED);
            }

            case INTERPRETER -> {
                date.setText("");
                user.setText("");
                device.setText("");
            }

            case NONE -> {

            }

        }

        switch (message.getMessageType()){
            case MESSAGE -> {
                content.setFill(Color.GREY);
            }

            case COMMAND, DEVICE_COMMAND, SYSTEM_COMMAND, CONTROL_COMMAND -> {
                if(device.getText().isEmpty()){
                    content.setText(message.getContent());
                } else {
                    content.setText(message.getContent().substring(message.getContent().indexOf(" ")));
                }
                content.setFill(Color.WHITE);
                device.setFill(Color.POWDERBLUE);
            }

            case ERROR -> {
                content.setFill(Color.RED);
            }

            case INFO -> {
                content.setFill(Color.SKYBLUE);
            }

            case NONE -> {

            }
        }

        TextFlow textFlow = new TextFlow();
        if(!date.getText().isEmpty()) textFlow.getChildren().add(date);
        if(!user.getText().isEmpty()) textFlow.getChildren().add(user);
        if(!device.getText().isEmpty()) textFlow.getChildren().add(device);
        textFlow.getChildren().add(content);

        textFlow.setPrefWidth(300);

        return textFlow;
    }

    private VBox generateDeviceBox(){
        VBox deviceBox = new VBox();
        deviceBox.setMinWidth(300);
        deviceBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        deviceBox.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 0, 3))));
        deviceBox.setPadding(new Insets(10));
        deviceBox.setSpacing(10);

        VBox deviceInfoVBox = new VBox();
        deviceInfoVBox.setSpacing(5);
        deviceInfoVBox.setPadding(new Insets(10, 0, 10, 0));
        deviceInfoVBox.setBorder(new Border(new BorderStroke(
                Paint.valueOf("white"),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 3, 0)
        )));
        deviceBox.getChildren().add(deviceInfoVBox);

        Text deviceName = new Text("No device");
        Text devicePort = new Text();
        Text deviceStateLabel = new Text();
        try{
            deviceName.setText(deviceManager.getDevices().get(0).getName());
            devicePort.setText("Serial port: " + deviceManager.getDevices().get(0).getPortName());
            deviceStateLabel.setText("Current state: " + deviceManager.getDevices().get(0).getCurrentState());
        } catch (IndexOutOfBoundsException e){
            logger.error(e.getMessage());
        }

        deviceName.setFont(new Font(25));
        deviceName.setTextAlignment(TextAlignment.CENTER);
        deviceName.setFill(Paint.valueOf("white"));
        deviceInfoVBox.getChildren().add(deviceName);

        deviceStateLabel.setFont(new Font(15));
        deviceStateLabel.setTextAlignment(TextAlignment.LEFT);
        deviceStateLabel.setFill(Paint.valueOf("white"));
        deviceInfoVBox.getChildren().add(deviceStateLabel);

        for(ExternalDevice device:deviceManager.getDevices()){
            ChangeListener<DeviceState> deviceStateChangeListener = (observableValue, deviceState, t1) -> {
                if (device.equals(deviceManager.getDevice(deviceManager.selectedDeviceIndex.get()))) {
                    if(!t1.getState().isBlank()){
                        deviceStateLabel.setText("Current state: " + t1.getState());
                    }
                }
            };
            deviceManager.getDeviceState(device).currentState.addListener(deviceStateChangeListener);
        }

        devicePort.setFont(new Font(12));
        devicePort.setTextAlignment(TextAlignment.LEFT);
        devicePort.setFill(Paint.valueOf("grey"));
        deviceInfoVBox.getChildren().add(devicePort);

        // Show device instruction set
        Text instructionSetLabel = new Text("Available commands:");
        instructionSetLabel.setFont(new Font(15));
        instructionSetLabel.setFill(Paint.valueOf("white"));
        deviceInfoVBox.getChildren().add(instructionSetLabel);

        VBox deviceInstructionSetVBox = new VBox();
        deviceInstructionSetVBox.getChildren().add(generateDeviceInstructionSet());
        deviceInfoVBox.getChildren().add(deviceInstructionSetVBox);


        // todo: remove instructions before changing device

        Text instructionSetTip = new Text("Type\n'!<device> <command> help' to learn more");
        instructionSetTip.setFont(new Font(12));
        instructionSetTip.setFill(Paint.valueOf("grey"));
        deviceInfoVBox.getChildren().add(instructionSetTip);

        deviceManager.selectedDeviceIndex.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() == -1) return;
                ExternalDevice device = deviceManager.getDevice((int) newValue);
                deviceName.setText(device.getName());
                devicePort.setText("Serial port: " + device.getPortName());
                deviceStateLabel.setText("Current state: " + device.getCurrentState());

                Platform.runLater(() -> {
                    deviceInstructionSetVBox.getChildren().clear();
                    deviceInstructionSetVBox.getChildren().add(generateDeviceInstructionSet());
                });
            }
        });

        deviceBox.getChildren().add(generateDeviceLogList());
        return deviceBox;
    }

    private VBox generateDeviceInstructionSet(){
        VBox instructions = new VBox();
        instructions.setSpacing(5);

        try{
            List<DeviceCommand> commands = deviceManager.getDevices().get(deviceManager.selectedDeviceIndex.get()).getCommands();
            for(DeviceCommand command:commands){
                Text instructionLabel = new Text(command.getPrefix() + " - " + command.getName());
                instructionLabel.setFont(new Font(12));
                instructionLabel.setFill(Paint.valueOf("white"));

                instructions.getChildren().add(instructionLabel);
            }

        }catch (IndexOutOfBoundsException e){
            logger.error(e.getMessage());
        }

        return instructions;
    }

    private ScrollPane generateDeviceLogList(){
        VBox messages = new VBox();
        // todo: temp solution to get rid of a white strip on a device log list when there are no logs
        Pane cell = new Pane();
        cell.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));

        Text content = new Text("");
        content.setWrappingWidth(250);
        content.setFill(Paint.valueOf("white"));

        Text dateText = new Text("");
        dateText.setFill(Paint.valueOf("white"));
        StackPane date = new StackPane(dateText);
        StackPane.setAlignment(dateText, Pos.CENTER_RIGHT);

        VBox contentVBox = new VBox(content, date);
        contentVBox.setPadding(new Insets(10, 10, 0, 10));
        contentVBox.prefWidthProperty().bind(cell.widthProperty());
        cell.getChildren().add(contentVBox);

        messages.getChildren().add(new Pane(cell));

        // todo: end of temp solution

        try{
            messages.getChildren().addAll(generateReceivedCommandsList(deviceManager.getDevice(0)));
        } catch (IndexOutOfBoundsException e){
            logger.error(e.getMessage());
        }

        messages.setSpacing(10);
        messages.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
//        messages.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));

        for(ExternalDevice device:deviceManager.getDevices()){
            ListChangeListener<ReceivedMessage> listChangeListener = c -> {
                if(device.equals(deviceManager.getDevice(deviceManager.selectedDeviceIndex.get()))){
                    messages.getChildren().clear();
                    messages.getChildren().addAll(generateReceivedCommandsList(device));
                }
            };

            deviceManager.getDeviceState(device).receivedMessages.addListener(listChangeListener);
        }

        deviceManager.selectedDeviceIndex.addListener((observable, oldValue, newValue) -> {
            if(newValue.intValue() == -1) return;

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    messages.getChildren().clear();
                    messages.getChildren().addAll(generateReceivedCommandsList(deviceManager.getDevice((int) newValue)));
                }
            });
        });

        ScrollPane scrollPane = new ScrollPane(messages);
        scrollPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    private ArrayList<Pane> generateReceivedCommandsList(ExternalDevice device){
        ArrayList<Pane> panes = new ArrayList<>();
        SimpleDateFormat receivedMessageTimestampDateFormat = new SimpleDateFormat("HH:mm:ss:SSS"); //todo: this could be read from the config file for device

        //todo: this list should be a stack so it doesn't need to be reversed
        ArrayList<ReceivedMessage> reversedReceivedMessages = new ArrayList<>();
        for (int i = deviceManager.getDeviceState(device).receivedMessages.size() - 1; i >= 0; i--) {
            reversedReceivedMessages.add(deviceManager.getDeviceState(device).receivedMessages.get(i));
        }

        for (ReceivedMessage receivedMessage : reversedReceivedMessages) {
            Pane cell = new Pane();
//            cell.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
            cell.setBorder(new Border(new BorderStroke(Paint.valueOf("grey"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0, 0, 1, 0))));

            Text content = new Text(receivedMessage.getContent());
            content.setWrappingWidth(250);
            content.setFill(Paint.valueOf("white"));

            Text dateText = new Text(receivedMessageTimestampDateFormat.format(receivedMessage.getTimestamp()));
            dateText.setFill(Paint.valueOf("grey"));
            StackPane date = new StackPane(dateText);
//            StackPane.setAlignment(dateText, Pos.CENTER_RIGHT);
            StackPane.setAlignment(dateText, Pos.CENTER_LEFT);

            VBox contentVBox = new VBox(content, date);
            contentVBox.setPadding(new Insets(0, 20, 0, 0));
            contentVBox.prefWidthProperty().bind(cell.widthProperty());
            cell.getChildren().add(contentVBox);

            panes.add(cell);
        }


        return panes;
    }

    private BorderPane generateStatusBar(){
        BorderPane statusBar = new BorderPane();
        statusBar.setMinHeight(40);
        statusBar.setMaxHeight(40);
        statusBar.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        statusBar.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 3, 0))));
//        statusBar.setSpacing(5);
        statusBar.setPadding(new Insets(5));

        HBox deviceHBox = new HBox();
        for(Device device:deviceManager.getDevices()){
            StackPane deviceTab = new StackPane();
            deviceTab.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0, 2, 0, 0))));
//            deviceTab.setBackground(new Background(new BackgroundFill(Paint.valueOf("grey"), CornerRadii.EMPTY, Insets.EMPTY)));
            Text deviceName = new Text(device.getName());
            deviceName.setFill(Paint.valueOf("white"));
            StackPane.setAlignment(deviceName, Pos.CENTER);
            deviceTab.getChildren().add(deviceName);
            deviceTab.setMinWidth(100);
            deviceTab.setMaxHeight(20);

            deviceHBox.getChildren().add(deviceTab);
        }

        statusBar.setLeft(deviceHBox);

        //todo: statusBar needs to be more precise and consist of space for only devices (?)
        deviceManager.selectedDeviceIndex.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() == -1) return;
                for(int i = 0; i < deviceHBox.getChildren().size(); i++){
                    Node n = deviceHBox.getChildren().get(i);
                    if(i == (int) newValue){
//                        ((StackPane) n).setBackground(new Background(new BackgroundFill(Paint.valueOf("grey"), CornerRadii.EMPTY, Insets.EMPTY)));
                        ((StackPane) n).setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0, 2, 0, 0))));
                    } else {
//                        ((StackPane) n).setBackground(new Background(new BackgroundFill(Paint.valueOf("blue"), CornerRadii.EMPTY, Insets.EMPTY)));
                    }
                }
            }
        });

        // todo: DRY
        for(int i = 0; i < deviceHBox.getChildren().size(); i++){
            Node n = deviceHBox.getChildren().get(i);
            if(i == (int) deviceManager.selectedDeviceIndex.get()){
//                ((StackPane) n).setBackground(new Background(new BackgroundFill(Paint.valueOf("grey"), CornerRadii.EMPTY, Insets.EMPTY)));
                ((StackPane) n).setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0, 2, 0, 0))));
            } else {
//                ((StackPane) n).setBackground(new Background(new BackgroundFill(Paint.valueOf("blue"), CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }

        HBox chatsHBox = new HBox();
        for(ChatService chat:chatManager.getChats()){
            HBox chatTab = new HBox();
            chatTab.setPadding(new Insets(5));
//            chatTab.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0, 0, 0, 2))));
            Text chatName = new Text(chat.getName());
            chatName.setFill(Paint.valueOf("white"));
            StackPane chatNameStackPane = new StackPane(chatName);
            StackPane.setAlignment(chatNameStackPane, Pos.CENTER);
            chatTab.getChildren().addAll(chatNameStackPane, new ImageView(chat.getIcon()));
            chatsHBox.getChildren().add(chatTab);
        }

        statusBar.setRight(chatsHBox);

        return statusBar;
    }

    @Override
    public void annotateMessage(Message message) {

    }

    @Override
    public void handleMessage(Message message) {
        messages.add(message);
        messages.sort(Comparator.comparing(Message::getTimestamp));
        if(messages.size() > 100) messages.subList(messages.size() - 100, messages.size()); // cap size at 100

        Platform.runLater(() -> {
            messageCellList.getChildren().clear();
            for(Message msg: messages){
                Pane cell = generateUserMessageCell(msg);
                cell.setRotate(180);
                messageCellList.getChildren().add(0, cell);
            }
        });
    }
}