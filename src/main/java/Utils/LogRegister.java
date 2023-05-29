package Utils;

import Devices.ReceivedMessage;
import StreamingService.Message;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogRegister {
    private final BufferedWriter writer;

    public LogRegister(String logFilePath){
        if(logFilePath.isEmpty()){
            writer = new BufferedWriter(new StringWriter());
            return;
        }

        if(Files.notExists(Path.of(logFilePath))){
            new File(logFilePath).mkdir();
        }

        try {
            writer = new BufferedWriter(
                    new FileWriter(
                            logFilePath +
                                    "chat-" +
                                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) +
                                    ".log"));

            // Add a shutdown hook to ensure that the writer is closed
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void log(String text) {
        try {
            writer.write(text + '\n');
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void log(Message message) {
        try {
            String date = new SimpleDateFormat("HH:mm:ss").format(message.getTimestamp());
            String user = message.getUser().getName();
            String content = message.getContent();

            writer.write("[" + date + "] " + user + ": " + content +'\n');
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void log(ReceivedMessage message) {
        try {
            String date = new SimpleDateFormat("HH:mm:ss").format(message.getTimestamp());
            String device = message.getDeviceName();
            String content = message.getContent();

            writer.write("[" + date + "] " + device + ": " + content +'\n');
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
