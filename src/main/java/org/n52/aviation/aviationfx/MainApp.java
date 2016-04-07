package org.n52.aviation.aviationfx;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.n52.aviation.aviationfx.consume.AmqpConsumer;


public class MainApp extends Application {

    private AmqpConsumer amqpConsumer;

    @Override
    public void start(Stage stage) throws Exception {
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
    }

}
