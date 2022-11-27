package View;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

// The presenter class
public class Camera {
    private final String name;
    private final String portName;
    private final Webcam webcam;
    private Task<Void> webcamTask; // todo: try to make it final
    private Thread thread;
    private final ImageView frameView;

    public Camera(String name, String portName) {
        this.name = name;
        this.portName = portName;

        webcam = Webcam.getWebcams().stream().filter(i -> i.getName().contains(portName)).findFirst().orElse(null); // todo: think how to notify about error when camera is null
        frameView = new ImageView(); // todo: display some hardcoded image to indicate that the camera is missing
        frameView.setUserData(name);
        frameView.setPreserveRatio(true);

        if(webcam == null) return;

        // todo: Is FHD smaller than HD? HD causes problems with USB controller having too little memory but FHD doesn't.
//        webcam.setCustomViewSizes(WebcamResolution.HD.getSize());
        webcam.setCustomViewSizes(WebcamResolution.FHD.getSize());

//        webcam.setViewSize(WebcamResolution.HD.getSize().getSize());
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        webcamTask = new Task<>() {
            final AtomicReference<WritableImage> frame = new AtomicReference<>();
            final ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
            BufferedImage bufferedFrame;

            @Override
            protected Void call() {
                webcam.open();
                frameView.imageProperty().bind(imageProperty);

                while(!isCancelled()){
                    try {
                        bufferedFrame = webcam.getImage();

                        if (bufferedFrame != null) {
                            frame.set(SwingFXUtils.toFXImage(bufferedFrame, frame.get()));
                            bufferedFrame.flush();
                            Platform.runLater(() -> imageProperty.set(frame.get()));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                webcam.close();
                return null;
            }
        };
    }

    public ImageView getFrameView() {
        return frameView;
    }

    public Webcam getWebcam(){
        return webcam;
    }

    public String getName() {
        return name;
    }

    public String getPortName() {
        return portName;
    }

    public void stop(){
        webcamTask.cancel();
    }

    public void start(){
        if(webcamTask == null) return;

        if(webcamTask.isRunning()){
            webcamTask.cancel();
        } else {
            // perhaps? good idea maybe?
//        webcamTask = new WebcamTask(webcamTask.getWebcam(), webcamTask.getImageView());
        }

        thread = new Thread(webcamTask);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public String toString() {
        return "Camera{" +
                "name='" + name + '\'' +
                ", portName='" + portName + '\'' +
                '}';
    }
}

