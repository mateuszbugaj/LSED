public class SystemCommandsTest {

//    @Test
//    public void changeCameraTest() throws SerialPortNotFoundException {
//        // Given
//        Camera camera1 = new Camera("Cam1", "/dev/cam1");
//        Camera camera2 = new Camera("Cam2", "/dev/cam2");
//        ExternalDevice externalDevice = new ExternalDevice("Dev1", new SerialCom(null, 9600), List.of(camera1, camera2), List.of(), "");
//
//        DeviceManager deviceManager = new DeviceManager();
//        deviceManager.addDevice(externalDevice);
//
//        ChatManager chatManager = new ChatManager();
//        deviceManager.addSubscriber(chatManager);
//        chatManager.addSubscriber(deviceManager);
//
//        // When
//        chatManager.update(new UserMessage(UserManager.getUser("user1"), "!sys cc Cam2", new Date()));
//
//        // Then
//        Assertions.assertEquals("Cam2", externalDevice.getSelectedCamera().getName());
//
//        // When
//        chatManager.update(new UserMessage(UserManager.getUser("user1"), "!sys cc 1", new Date()));
//
//        // Then
//        Assertions.assertEquals("Cam1", externalDevice.getSelectedCamera().getName());
//    }
}
