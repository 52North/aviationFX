package org.n52.aviation.aviationfx;

import com.sun.javafx.stage.StageHelper;
import java.util.Random;
import java.util.Scanner;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.n52.amqp.ContentType;
import org.n52.aviation.aviationfx.consume.AmqpConsumer;
import org.n52.aviation.aviationfx.consume.NewMessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainApp extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);
    private AmqpConsumer amqpConsumer;
    private Stage mainStage;
    private double currLat = 35.15;
    private double currLon = -119.38;

    @Override
    public void start(Stage stage) throws Exception {
        this.mainStage = stage;
        initEventBusComponents();

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Scene.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("52°North aviationFX");
        stage.setScene(scene);
        stage.show();

    }

    @Override
    public void stop() throws Exception {
        this.amqpConsumer.shutdown();
        if (this.mainStage != null) {
            this.mainStage.close();
        }
        else {
            LOG.warn("Stage is already null!");
        }
    }


    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private void initEventBusComponents() {
        this.amqpConsumer = new AmqpConsumer();

        //TODO do not use singletons....
        EventBusInstance.getEventBus().register(this.amqpConsumer);

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
            EventBusInstance.getEventBus().post(new NewMessageEvent(readAirspace(), ContentType.APPLICATION_XML));

            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
                EventBusInstance.getEventBus().post(new NewMessageEvent(readFlight(), ContentType.APPLICATION_XML));
            }
        })
//        .start()
        ;
    }

    private String readAirspace() {
        StringBuilder sb;
        try (Scanner sc = new Scanner(getClass().getResourceAsStream("/test-saa.xml"))) {
            sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }
        }
        return sb.toString();
    }

    private String readFlight() {
        StringBuilder sb;
        try (Scanner sc = new Scanner(getClass().getResourceAsStream("/test-flight.xml"))) {
            sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }   this.currLat += 0.01;
            this.currLon += 0.01;
        }
        return sb.toString().replace("${lat}", ""+currLat).replace("${lon}", ""+currLon).replace("${bearing}", ""+new Random().nextInt(359));
    }
}
