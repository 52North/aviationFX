package org.n52.aviation.aviationfx;

import com.google.common.eventbus.Subscribe;
import com.lynden.gmapsfx.GoogleMapView;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.n52.aviation.aviationfx.consume.NewMessageEvent;
import org.n52.aviation.aviationfx.maps.GMapsMapComponent;
import org.n52.aviation.aviationfx.maps.MapComponent;
import org.n52.aviation.aviationfx.subscribe.NewSubscriptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @FXML
    private AnchorPane mapWrapper;

    @FXML
    private GoogleMapView gmapView;

    @FXML
    private MenuBar menu;

    @FXML
    private MenuItem menuExit;

    @FXML
    private Button addSubscription;

    @FXML
    private ListView<String> subscriptionList;

    private MapComponent mapComponent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        menu.setUseSystemMenuBar(true);
        menuExit.setOnAction(e -> Platform.exit());
        gmapView = new GoogleMapView("/html/map.html");

        this.mapComponent = new GMapsMapComponent(this.gmapView);
        this.mapComponent.initialize();

        AnchorPane.setTopAnchor(gmapView, 0.0);
        AnchorPane.setBottomAnchor(gmapView, 0.0);
        AnchorPane.setLeftAnchor(gmapView, 0.0);
        AnchorPane.setRightAnchor(gmapView, 0.0);
        mapWrapper.getChildren().add(gmapView);

        addSubscription.setOnAction(e -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/NewSubscription.fxml"));

                Scene scene = new Scene(root);
                scene.getStylesheets().add("/styles/Styles.css");
                //make another stage for scene2
                Stage newStage = new Stage();
                newStage.setTitle("Add Subscription");
                newStage.setScene(scene);
                //tell stage it is meannt to pop-up (Modal)
                newStage.initModality(Modality.APPLICATION_MODAL);
                newStage.setTitle("Pop up window");
                //rest of code -
                newStage.showAndWait();
            } catch (IOException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        });

        EventBusInstance.getEventBus().register(this);
    }

    @Subscribe
    public void onNewSubscription(NewSubscriptionEvent event) {
        this.subscriptionList.getItems().add(event.getProperties().getId());
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent event) {
        this.mapComponent.onNewMessage(event);
    }

}
