package org.n52.aviation.aviationfx;

import com.google.common.eventbus.Subscribe;
import com.lynden.gmapsfx.GoogleMapView;
import static com.sun.javafx.animation.TickCalculation.sub;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.event.EventType;
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
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import net.opengis.pubsub.x10.DeliveryMethodDocument;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.PublicationIdentifierDocument;
import net.opengis.pubsub.x10.SubscriptionIdentifierDocument;
import net.opengis.pubsub.x10.UnsubscribeResponseDocument;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.aviation.aviationfx.consume.NewMessageEvent;
import org.n52.aviation.aviationfx.maps.GMapsMapComponent;
import org.n52.aviation.aviationfx.maps.MapComponent;
import org.n52.aviation.aviationfx.subscribe.NewSubscriptionEvent;
import org.n52.aviation.aviationfx.subscribe.SubscribeFailedException;
import org.n52.aviation.aviationfx.subscribe.SubscriptionProperties;
import org.oasisOpen.docs.wsn.b2.ConsumerReferenceDocument;
import org.oasisOpen.docs.wsn.b2.SubscribeDocument;
import org.oasisOpen.docs.wsn.b2.SubscribeResponseDocument;
import org.oasisOpen.docs.wsn.b2.UnsubscribeDocument;
import org.oasisOpen.docs.wsn.b2.UnsubscribeResponseDocument.UnsubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.ReferenceParametersType;

public class MainController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    @FXML
    private AnchorPane mainAnchorPane;

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
    private final Map<String, SubscriptionProperties> subscriptions = new HashMap<>();
    private AtomicBoolean initialized = new AtomicBoolean(false);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mainAnchorPane.addEventHandler(EventType.ROOT, e -> {
            if (initialized.getAndSet(true)) {
                return;
            }

            Window stage = mainAnchorPane.getScene().getWindow();
            EventHandler<WindowEvent> originalClose = stage.getOnCloseRequest();
            stage.setOnCloseRequest(closeEvent -> {
                shutdownResources(originalClose, closeEvent);
            });

            LOG.info("Setup close event handler finished!");
        });


        menu.setUseSystemMenuBar(true);
        menuExit.setOnAction(e -> {
            shutdownResources(null, null);
            Platform.exit();
        });

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

    private void shutdownResources(EventHandler<WindowEvent> originalClose, WindowEvent closeEvent) {
        LOG.info("MainController closing event received");

        unsubscribeAll();
        LOG.info("unsubscring finished.");

        if (originalClose != null) {
            originalClose.handle(closeEvent);
        }
    }

    @Subscribe
    public void onNewSubscription(NewSubscriptionEvent event) {
        String id = event.getProperties().getId();
        this.subscriptionList.getItems().add(id);
        this.subscriptions.put(id, event.getProperties());
    }

    @Subscribe
    public void onNewMessage(NewMessageEvent event) {
        this.mapComponent.onNewMessage(event);
    }

    private void unsubscribeAll() {
        this.subscriptions.keySet().stream().forEach(c -> {
            try {
                unsubscribe(c);
            }
            catch (RuntimeException e) {
                LOG.warn(e.getMessage(), e);
            }
        });
    }

    private void unsubscribe(String subId) {
        SubscriptionProperties props = this.subscriptions.get(subId);

        UnsubscribeDocument unsubDoc = UnsubscribeDocument.Factory.newInstance();
        UnsubscribeDocument.Unsubscribe unsub = unsubDoc.addNewUnsubscribe();

        SubscriptionIdentifierDocument subIdDoc = SubscriptionIdentifierDocument.Factory.newInstance();
        subIdDoc.setSubscriptionIdentifier(subId);

        XmlBeansHelper.insertChild(unsub, subIdDoc);

        EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
        Body body = envDoc.addNewEnvelope().addNewBody();
        body.set(unsubDoc);

        try (CloseableHttpClient c = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(props.getPubSubHost());
            post.setEntity(new StringEntity(envDoc.xmlText(new XmlOptions().setSavePrettyPrint())));
            post.setHeader("Content-Type", "application/soap+xml");

            CloseableHttpResponse resp = c.execute(post);
            XmlObject xo = XmlObject.Factory.parse(resp.getEntity().getContent());
            if (!(xo instanceof EnvelopeDocument)) {
                LOG.warn("Could not process response: "+xo.xmlText());
                return;
            }

            envDoc = (EnvelopeDocument) xo;
            XmlCursor cur = envDoc.getEnvelope().getBody().newCursor();
            cur.toFirstChild();
            XmlObject respBody = cur.getObject();

            if (!(respBody instanceof UnsubscribeResponse)) {
                LOG.warn("Not a valid subscribe response: "+xo.xmlText());
            }
            else {
                LOG.info("Unsubscribed '{}'", subId);
            }

            Platform.runLater(() -> {
                subscriptionList.getItems().remove(subId);
            });
        } catch (IOException | XmlException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

}
