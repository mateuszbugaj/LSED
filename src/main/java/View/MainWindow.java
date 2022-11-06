package View;

import Device.Device;
import Device.DeviceManager;
import Device.ReceivedMessage;
import StreamingService.Chat;
import StreamingService.ChatManager;
import StreamingService.MessageType;
import StreamingService.UserMessage;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
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
import java.util.List;

// The view class
// Each component of the GUI should be its own object
public class MainWindow {
    private static final Logger logger = LoggerFactory.getLogger(MainWindow.class);
    private final Stage stage;
    private final DeviceManager deviceManager;
    private final ChatManager chatManager;

    public MainWindow(Stage s, DeviceManager deviceManager, ChatManager chatManager){
        stage = s;
        this.deviceManager = deviceManager;
        this.chatManager = chatManager;

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

    private void showMainBorderPane(Stage stage){
        BorderPane mainBorderPane = new BorderPane();
        Scene scene = new Scene(mainBorderPane);
        stage.setScene(scene);

        mainBorderPane.setCenter(generateViewBox());
        mainBorderPane.setLeft(generateUserBox());
    }

    private BorderPane generateViewBox(){
        BorderPane viewBox = new BorderPane();
        viewBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        viewBox.setPadding(new Insets(0, 0, 10, 0));

        Device device;
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
                Device device = deviceManager.getDevice((int) newValue);
                ArrayList<Camera> cameras = device.getCameras();

                mainFramePane.getChildren().clear();
                mainFramePane.getChildren().add(cameras.get(0).getFrameView());

                cameras.get(0).getFrameView().fitWidthProperty().bind(mainFramePane.widthProperty());

                viewBox.setCenter(mainFramePane);
                viewBox.setBottom(generateThumbnails(cameras));

                deviceBorderPane.setCenter(viewBox);
            }
        });

        deviceBorderPane.setTop(generateStatusBar());
        deviceBorderPane.setRight(generateDeviceBox());

        return deviceBorderPane;
    }

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

    private VBox generateUserBox(){
        VBox userBox = new VBox();
        userBox.setMinWidth(300);
        userBox.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        userBox.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 3, 0, 0))));
        userBox.setPadding(new Insets(10));
        userBox.setSpacing(10);

        // Info about user in charge or current event ect.

        // All user messages (or only valid commands?)
        VBox commands = new VBox();
        commands.setSpacing(10);
        commands.setPrefHeight(2000);
        commands.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        SimpleDateFormat receivedMessageTimestampDateFormat = new SimpleDateFormat("HH:mm:ss:SSS"); //todo: this could be read from the config file for device

        // todo: this may be not needed as there is no messages from users at the start of the program
        for(UserMessage userMessage: chatManager.getChatMessages()){
            Pane cell = generateUserMessageCell(userMessage);
            cell.setRotate(180);
            commands.getChildren().add(0, cell);
        }

        chatManager.getChatMessages().addListener(new ListChangeListener<UserMessage>() {
            @Override
            public void onChanged(Change<? extends UserMessage> c) {
                Platform.runLater(() -> {
                    commands.getChildren().clear();

                    // todo: DRY
                    for(UserMessage userMessage: chatManager.getChatMessages()){
                        Pane cell = generateUserMessageCell(userMessage);
                        cell.setRotate(180);
                        commands.getChildren().add(0, cell);
                    }
                });
            }
        });

        ScrollPane scrollPane = new ScrollPane(commands);
        scrollPane.setPrefHeight(2000);
        scrollPane.setRotate(180);
        scrollPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        userBox.getChildren().add(scrollPane);

        return userBox;
    }

    private TextFlow generateUserMessageCell(UserMessage userMessage){
        Text date = new Text();
        Text user = new Text();
        Text device = new Text();
        Text content = new Text();

        switch (userMessage.getMessageType()){
            case USER_MESSAGE -> {
                date = new Text("[" + new SimpleDateFormat("HH:mm:ss").format(userMessage.getTimestamp()) + "] ");
                date.setFill(Color.LIGHTSLATEGREY);

                user = new Text(userMessage.getUser() + ": ");
                user.setFill(Color.MEDIUMSEAGREEN);

                content = new Text(" $ " + userMessage.getContent());
                content.setFill(Color.GREY);
            }

            case USER_COMMAND -> {
                date = new Text("[" + new SimpleDateFormat("HH:mm:ss").format(userMessage.getTimestamp()) + "] ");
                date.setFill(Color.LIGHTSLATEGREY);

                user = new Text(userMessage.getUser() + ": ");
                user.setFill(Color.MEDIUMSEAGREEN);

                if(userMessage.getTargetDevice() == null){
                    device = new Text("/");
                } else {
                    device = new Text("/" + userMessage.getTargetDevice().getDeviceName());
                }
                device.setFill(Color.POWDERBLUE);

                content = new Text(" $ " + userMessage.getContent());
                content.setFill(Color.WHITE);
            }

            case ADMIN_MESSAGE -> {
                date = new Text("[" + new SimpleDateFormat("HH:mm:ss").format(userMessage.getTimestamp()) + "] ");
                date.setFill(Color.LIGHTSLATEGREY);

                user = new Text(userMessage.getUser());
                user.setFill(Color.TOMATO);

                //todo: Should admin message contain info about the device
//                if(userMessage.getTargetDevice() == null){
//                    device = new Text("/");
//                } else {
//                    device = new Text("/" + userMessage.getTargetDevice().getDeviceName());
//                }
//                device.setFill(Color.POWDERBLUE);

                content = new Text("$ " + userMessage.getContent());
                content.setFill(Color.WHITE);
            }

            case INTERPRETER_MESSAGE -> {
                System.out.println(userMessage.getContent());
                content = new Text(userMessage.getContent());
                content.setFill(Color.TOMATO);
            }

            case NONE -> {
                date = new Text("[" + new SimpleDateFormat("HH:mm:ss").format(userMessage.getTimestamp()) + "] ");
                date.setFill(Color.LIGHTSLATEGREY);

                user = new Text(userMessage.getUser() + ": ");
                user.setFill(Color.GREY);

                content = new Text(userMessage.getContent());
                content.setFill(Color.GREY);
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

        StackPane deviceNameStackPane = new StackPane();
        deviceNameStackPane.setBorder(new Border(new BorderStroke(
                Paint.valueOf("white"),
                BorderStrokeStyle.SOLID,
                CornerRadii.EMPTY,
                new BorderWidths(0, 0, 3, 0)
        )));

        Text deviceName = new Text("No device");
        Text devicePort = new Text();
        try{
            deviceName.setText(deviceManager.getDevices().get(0).getDeviceName());
            devicePort.setText(deviceManager.getDevices().get(0).getPortName());
        } catch (IndexOutOfBoundsException e){
            logger.error(e.getMessage());
        }

        deviceName.setFont(new Font(25));
        deviceName.setTextAlignment(TextAlignment.CENTER);
        deviceName.setFill(Paint.valueOf("white"));
        deviceNameStackPane.getChildren().add(deviceName);
        deviceBox.getChildren().add(deviceNameStackPane);

        devicePort.setFont(new Font(15));
        devicePort.setTextAlignment(TextAlignment.LEFT);
        devicePort.setFill(Paint.valueOf("white"));
        deviceBox.getChildren().add(devicePort);

        deviceManager.selectedDeviceIndex.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Device device = deviceManager.getDevice((int) newValue);
                deviceName.setText(device.getDeviceName());
                devicePort.setText(device.getPortName());
            }
        });

        deviceBox.getChildren().add(generateDeviceLogList());
        return deviceBox;
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

        for(Device device:deviceManager.getDevices()){
            ListChangeListener<ReceivedMessage> listChangeListener = c -> {
                if(device.equals(deviceManager.getDevice(deviceManager.selectedDeviceIndex.get()))){
                    messages.getChildren().clear();
                    messages.getChildren().addAll(generateReceivedCommandsList(device));
                }
            };

            deviceManager.getDeviceState(device).receivedMessages.addListener(listChangeListener);
        }

        deviceManager.selectedDeviceIndex.addListener((observable, oldValue, newValue) -> {
            messages.getChildren().clear();
            messages.getChildren().addAll(generateReceivedCommandsList(deviceManager.getDevice((int) newValue)));
        });

        ScrollPane scrollPane = new ScrollPane(messages);
        scrollPane.setBackground(new Background(new BackgroundFill(Paint.valueOf("black"), CornerRadii.EMPTY, Insets.EMPTY)));
        scrollPane.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5))));
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        return scrollPane;
    }

    private ArrayList<Pane> generateReceivedCommandsList(Device device){
        ArrayList<Pane> panes = new ArrayList<>();
        SimpleDateFormat receivedMessageTimestampDateFormat = new SimpleDateFormat("HH:mm:ss:SSS"); //todo: this could be read from the config file for device

        //todo: this list should be a stack so it doesn't need to be reversed
        ArrayList<ReceivedMessage> reversedReceivedMessages = new ArrayList<>();
        for (int i = deviceManager.getDeviceState(device).receivedMessages.size() - 1; i >= 0; i--) {
            reversedReceivedMessages.add(deviceManager.getDeviceState(device).receivedMessages.get(i));
        }

        for (ReceivedMessage receivedMessage : reversedReceivedMessages) {
            Pane cell = new Pane();
            cell.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));

            Text content = new Text(receivedMessage.getMessage());
            content.setWrappingWidth(250);
            content.setFill(Paint.valueOf("white"));

            Text dateText = new Text(receivedMessageTimestampDateFormat.format(receivedMessage.getTimestamp()));
            dateText.setFill(Paint.valueOf("white"));
            StackPane date = new StackPane(dateText);
            StackPane.setAlignment(dateText, Pos.CENTER_RIGHT);

            VBox contentVBox = new VBox(content, date);
            contentVBox.setPadding(new Insets(10, 10, 0, 10));
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
            Text deviceName = new Text(device.getDeviceName());
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
        for(Chat chat:chatManager.getChats()){
            HBox chatTab = new HBox();
            chatTab.setPadding(new Insets(5));
//            chatTab.setBorder(new Border(new BorderStroke(Paint.valueOf("white"), BorderStrokeStyle.SOLID, new CornerRadii(0), new BorderWidths(0, 0, 0, 2))));
            Text chatName = new Text(chat.getChatName());
            chatName.setFill(Paint.valueOf("white"));
            StackPane chatNameStackPane = new StackPane(chatName);
            StackPane.setAlignment(chatNameStackPane, Pos.CENTER);
            chatTab.getChildren().addAll(chatNameStackPane, new ImageView(chat.getIcon()));
            chatsHBox.getChildren().add(chatTab);
        }

        statusBar.setRight(chatsHBox);

        return statusBar;
    }
}