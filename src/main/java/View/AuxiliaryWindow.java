package View;

import Device.Device;
import Device.DeviceManager;
import Device.ReceivedMessage;
import State.DeviceState;
import StreamingService.Chat;
import StreamingService.ChatManager;
import StreamingService.UserMessage;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Date;

public class AuxiliaryWindow {
    private final Stage stage;
    private final DeviceManager deviceManager;
    private final ChatManager chatManager;

    public AuxiliaryWindow(DeviceManager deviceManager, ChatManager chatManager) {
        this.deviceManager = deviceManager;
        this.chatManager = chatManager;

        stage = new Stage();
        stage.setTitle("LSED Options");
        stage.centerOnScreen();
    }

    /*
    Options window needs to be divided into areas dedicated to:
    - Devices
        - Sending commands
        - modifying options

    - Streaming Services
        - Logging into services to connect with chats

    - Users
        - Banning users
        - Granting privileges to certain devices

     */
    public void show(){
        VBox options = new VBox();
        options.setMinWidth(400);
        options.setPrefWidth(400);
        options.setPadding(new Insets(10));
        options.setSpacing(10);
        options.getChildren().add(generateDeviceOptions());
        options.getChildren().add(generateStreamingOptions());
        options.getChildren().add(generateUsersOptions());

        Scene scene = new Scene(options);
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.show();
    }

    public VBox generateDeviceOptions(){
        VBox devicesVBox = new VBox();
        devicesVBox.setMinHeight(200);
        devicesVBox.setSpacing(10);
        StackPane title = new StackPane(new Text("Devices"));
        title.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 3, 0))));
        devicesVBox.getChildren().add(title);

        HBox devicesHBox = new HBox();
        devicesHBox.setSpacing(5);
        for(int i = 0; i < deviceManager.getDevices().size(); i++){
            int finalI = i;
            Button deviceButton = new Button(deviceManager.getDevice(i).getDeviceName());
            deviceButton.setOnAction(event -> deviceManager.selectedDeviceIndex.set(finalI));
            devicesHBox.getChildren().add(deviceButton);
        }
        devicesVBox.getChildren().add(devicesHBox);

        Text deviceNameText = new Text();
        StackPane selectedDeviceName = new StackPane(deviceNameText);
        selectedDeviceName.setAlignment(Pos.CENTER_LEFT);
        deviceNameText.textProperty().bindBidirectional(deviceManager.selectedDeviceIndex, new StringConverter<>() {
            @Override
            public String toString(Number object) {
                return deviceManager.getDevices().get((Integer) object).getDeviceName();
            }

            @Override
            public Number fromString(String string) {
                return null;
            }
        });
        devicesVBox.getChildren().add(selectedDeviceName);

        TextArea deviceCommandInput = new TextArea();
        deviceCommandInput.setMaxHeight(100);
        deviceCommandInput.setPromptText("Input...");
        deviceCommandInput.setWrapText(true);
        BooleanProperty enterPressed = new SimpleBooleanProperty(false);
        BooleanProperty shiftPressed = new SimpleBooleanProperty(false);

        deviceCommandInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) enterPressed.setValue(true);
            if (event.getCode() == KeyCode.SHIFT) shiftPressed.setValue(true);
            if (enterPressed.get() && shiftPressed.get()){
                deviceCommandInput.setText(deviceCommandInput.getText().concat("\n"));
                deviceCommandInput.positionCaret(deviceCommandInput.getText().length());
            } else {
                if(enterPressed.get()){
                    if(enterPressed.get() && deviceCommandInput.getText().length() > 1){ // todo: it is possible to send only a new line character and it shouldn't
                        //todo: DRY
                        deviceManager.getDeviceSendCommand(deviceManager.selectedDeviceIndex.get()).run(deviceCommandInput.getText());
                        deviceCommandInput.setText("");
                    }
                }
            }
        });

        deviceCommandInput.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER) enterPressed.setValue(false);
            if(event.getCode() == KeyCode.SHIFT) shiftPressed.setValue(false);
        });

        TextArea deviceCommandOutput = new TextArea();
        deviceCommandOutput.setMaxHeight(100);
        deviceCommandOutput.setWrapText(true);
        deviceCommandOutput.setEditable(false);
        deviceCommandOutput.setFocusTraversable(false);
        deviceCommandOutput.setText(generateDeviceCommandOutputText(deviceManager.getDeviceState(deviceManager.selectedDeviceIndex.get())));

        /*
        Element showing device output is updated with every change of selected device and displays its received commands list.
        Observable list containing received commands for every device located in corresponding DeviceState has listener
        added which for every change checks if added command is directed to selected device. If so, the element is updated
        to show updated list of commands.
         */

        deviceManager.selectedDeviceIndex.addListener((observable, oldValue, newValue) -> {
            deviceCommandOutput.setText(generateDeviceCommandOutputText(deviceManager.getDeviceState((Integer) newValue)));
        });

        for(Device device:deviceManager.getDevices()){
            ListChangeListener<ReceivedMessage> listChangeListener = c -> {
                if(device.equals(deviceManager.getDevice(deviceManager.selectedDeviceIndex.get()))){
                    deviceCommandOutput.setText(generateDeviceCommandOutputText(deviceManager.getDeviceState(device)));
                }
            };

            deviceManager.getDeviceState(device).receivedMessages.addListener(listChangeListener);
        }

        HBox deviceCommandInputAndOutputHBox = new HBox(deviceCommandInput, deviceCommandOutput);
        deviceCommandInputAndOutputHBox.setSpacing(5);
        devicesVBox.getChildren().add(deviceCommandInputAndOutputHBox);

        TextField deviceCommandInputFilePathTextField = new TextField();
        deviceCommandInputFilePathTextField.setPromptText("File path");
        deviceCommandInputFilePathTextField.setPrefWidth(1000);

        Button sendCommandButton = new Button("Send");
        sendCommandButton.setMinWidth(100);
        sendCommandButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!deviceCommandInput.getText().isEmpty()){
                    deviceManager.getDeviceSendCommand(deviceManager.selectedDeviceIndex.get()).run(deviceCommandInput.getText());
                    deviceCommandInput.setText("");
                }
            }
        });
        HBox sendButtonAndFilePathHBox = new HBox(sendCommandButton, deviceCommandInputFilePathTextField);
        sendButtonAndFilePathHBox.setSpacing(10);
        devicesVBox.getChildren().add(sendButtonAndFilePathHBox);

        return devicesVBox;
    }

    private String generateDeviceCommandOutputText(DeviceState deviceState){
        String output = "";
        for(int i = deviceState.receivedMessages.size() - 1; i >= 0; i--){
            output = output.concat(deviceState.receivedMessages.get(i).getMessage()).concat("\n");
        }

        return output;
    }

    public VBox generateStreamingOptions(){
        VBox streamingVBox = new VBox();
        streamingVBox.setMinHeight(200);
        streamingVBox.setSpacing(10);
        StackPane title = new StackPane(new Text("Streaming Services"));
        title.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 3, 0))));
        streamingVBox.getChildren().add(title);

        HBox chatHBox = new HBox();
        chatHBox.setSpacing(10);
        for(Chat chat: chatManager.getChats()){
            HBox chatCell = new HBox(new Text(chat.getChatName()), new ImageView(chat.getIcon()));
            chatCell.setPadding(new Insets(3, 10, 3, 10));
            chatCell.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(2))));
            chatHBox.getChildren().add(chatCell);
        }
        streamingVBox.getChildren().add(chatHBox);

        TextField chatInputTextField = new TextField();
        chatInputTextField.setPromptText("Chat message...");
        chatInputTextField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                if(!chatInputTextField.getText().isEmpty()){
//                    chatManager.getChatMessages().add(new UserMessage("Admin", chatInputTextField.getText(), new Date()));
                    chatManager.sendMessage(new UserMessage("Admin", chatInputTextField.getText(), new Date()));
                    chatInputTextField.setText("");
                }
            }
        });
        streamingVBox.getChildren().add(chatInputTextField);

        Button sendChatInputButton = new Button("Send");
        sendChatInputButton.setMinWidth(100);
        sendChatInputButton.setOnAction(event -> {
            if(!chatInputTextField.getText().isEmpty()){
//                chatManager.getChatMessages().add(new UserMessage("Admin", chatInputTextField.getText(), new Date()));
                chatManager.sendMessage(new UserMessage("Admin", chatInputTextField.getText(), new Date()));
                chatInputTextField.setText("");
            }
        });
        streamingVBox.getChildren().add(sendChatInputButton);

        return streamingVBox;
    }

    public VBox generateUsersOptions(){
        VBox usersVBox = new VBox();
        usersVBox.setMinHeight(200);
        usersVBox.setSpacing(10);
        StackPane title = new StackPane(new Text("Users"));
        title.setBorder(new Border(new BorderStroke(Paint.valueOf("black"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 3, 0))));
        usersVBox.getChildren().add(title);

        return usersVBox;
    }
}
