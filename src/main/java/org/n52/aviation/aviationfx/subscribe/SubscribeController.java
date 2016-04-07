package org.n52.aviation.aviationfx.subscribe;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javax.xml.namespace.QName;
import net.opengis.pubsub.x10.DeliveryMethodDocument;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.PublicationIdentifierDocument;
import net.opengis.pubsub.x10.PublicationType;
import net.opengis.pubsub.x10.PublisherCapabilitiesDocument;
import net.opengis.pubsub.x10.SubscriptionIdentifierDocument;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.aviation.aviationfx.EventBusInstance;
import org.n52.aviation.aviationfx.XmlBeansHelper;
import org.oasisOpen.docs.wsn.b2.ConsumerReferenceDocument;
import org.oasisOpen.docs.wsn.b2.SubscribeDocument;
import org.oasisOpen.docs.wsn.b2.SubscribeResponseDocument.SubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.ReferenceParametersType;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class SubscribeController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(SubscribeController.class);

    @FXML
    private Accordion accordion;
    @FXML
    private TitledPane serverPane;
    @FXML
    private Button requestButton;
    @FXML
    private ComboBox<String> publication;
    @FXML
    private TextField pubSubServer;
    @FXML
    private ProgressIndicator requestProgress;
    @FXML
    private ComboBox<String> deliveryMethod;
    @FXML
    private Label publicationAbstract;
    @FXML
    private Label deliveryMethodAbstract;
    @FXML
    private Button cancelButton;
    @FXML
    private Button okButton;
    @FXML
    private ProgressIndicator okIndicator;

    private Map<String, String> publicationsMap = new HashMap<>();
    private Map<String, String> deliveryMethodsMap = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        accordion.setExpandedPane(serverPane);

        cancelButton.setOnAction(e -> {
            ((Node) (e.getSource())).getScene().getWindow().hide();
        });

        okButton.setOnAction(e -> {
            //show indicator
            Platform.runLater(() -> {
                okIndicator.setVisible(true);
            });

            String dm = deliveryMethod.valueProperty().get();
            String pub = publication.valueProperty().get();

            try {
                SubscriptionProperties props = subscribe(pubSubServer.getText(), pub, dm);
                EventBusInstance.getEventBus().post(new NewSubscriptionEvent(props));
            } catch (SubscribeFailedException ex) {
                EventBusInstance.getEventBus().post(new SubscribeFailedEvent(ex));
                LOG.warn(ex.getMessage(), ex);
            }

            ((Node) (e.getSource())).getScene().getWindow().hide();
        });

        requestButton.setOnAction(e -> {
            //show indicator
            Platform.runLater(() -> {
                requestProgress.setVisible(true);
            });

            new Thread(() -> {
                String serverUrl = pubSubServer.getText();

                serverUrl = serverUrl.concat("?service=PubSub&request=GetCapabilities");
                try (CloseableHttpClient c = HttpClientBuilder.create().build()) {
                    HttpGet get = new HttpGet(serverUrl);

                    CloseableHttpResponse resp = c.execute(get);
                    PublisherCapabilitiesDocument pubCapDoc = PublisherCapabilitiesDocument.Factory.parse(resp.getEntity().getContent());

                    //update UI
                    Platform.runLater(() -> {
                        updatePublications(pubCapDoc);
                        updateDeliveryMethods(pubCapDoc);
                        publication.setDisable(false);
                        requestProgress.setVisible(false);
                    });

                } catch (IOException | XmlException ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }).start();

        });
    }

    private void updateDeliveryMethods(PublisherCapabilitiesDocument pubCapDoc) {
        DeliveryMethodType[] deliveryArray = pubCapDoc.getPublisherCapabilities().getDeliveryCapabilities().getDeliveryMethodArray();
        if (deliveryArray != null) {
            for (DeliveryMethodType dm : deliveryArray) {
                String abstr = dm.getAbstractArray(0).getStringValue();
                String id = dm.getIdentifier();
                deliveryMethod.getItems().add(id);
                deliveryMethodsMap.put(id, abstr);
            }

            deliveryMethod.valueProperty().addListener(e -> {
                String id = deliveryMethod.valueProperty().get();
                deliveryMethodAbstract.setText(deliveryMethodsMap.get(id));
            });
        }
    }

    private void updatePublications(PublisherCapabilitiesDocument pubCapDoc) {
        PublicationType[] pubArray = pubCapDoc.getPublisherCapabilities().getPublications().getPublicationArray();
        if (pubArray != null) {
            for (PublicationType p : pubArray) {
                String id = p.getIdentifier();
                String abstr = p.getAbstractArray(0).getStringValue();
                publication.getItems().add(id);
                publicationsMap.put(id, abstr);
            }

            publication.valueProperty().addListener(e -> {
                String id = publication.valueProperty().get();
                publicationAbstract.setText(publicationsMap.get(id));
            });
        }
    }

    private SubscriptionProperties subscribe(String host, String pubId, String deliveryMethod) throws SubscribeFailedException {
        SubscribeDocument subDoc = SubscribeDocument.Factory.newInstance();
        SubscribeDocument.Subscribe sub = subDoc.addNewSubscribe();

        EndpointReferenceType conRef = sub.addNewConsumerReference();
        AttributedURIType addr = conRef.addNewAddress();
        addr.setStringValue("localhost");

        PublicationIdentifierDocument pubIdDoc = PublicationIdentifierDocument.Factory.newInstance();
        pubIdDoc.setPublicationIdentifier(pubId);

        XmlBeansHelper.insertChild(sub, pubIdDoc);

        DeliveryMethodDocument delMethDoc = DeliveryMethodDocument.Factory.newInstance();
        DeliveryMethodType delMeth = delMethDoc.addNewDeliveryMethod();
        delMeth.setIdentifier(deliveryMethod);

        XmlBeansHelper.insertChild(sub, delMethDoc);

        EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
        Body body = envDoc.addNewEnvelope().addNewBody();
        body.set(subDoc);

        try (CloseableHttpClient c = HttpClientBuilder.create().build()) {
            HttpPost post = new HttpPost(host);
            post.setEntity(new StringEntity(envDoc.xmlText(new XmlOptions().setSavePrettyPrint())));
            post.setHeader("Content-Type", "application/soap+xml");

            CloseableHttpResponse resp = c.execute(post);
            envDoc = EnvelopeDocument.Factory.parse(resp.getEntity().getContent());
            XmlCursor cur = envDoc.getEnvelope().getBody().newCursor();
            cur.toFirstChild();
            SubscribeResponse subRespDoc = (SubscribeResponse) cur.getObject();

            //TODO parse the subID
            ReferenceParametersType refParams = subRespDoc.getSubscriptionReference().getReferenceParameters();

            Stream<XmlObject> subIdDoc = XmlBeansHelper.findChildren(SubscriptionIdentifierDocument.type.getDocumentElementName(), refParams);
            Optional<XmlObject> first = subIdDoc.findFirst();
            String subId;
            if (first.isPresent()) {
                subId = XmlBeansHelper.extractStringContent(first.get());
            }
            else {
                throw new SubscribeFailedException("No subscription id in response");
            }

            Stream<XmlObject> queueElem = XmlBeansHelper.findChildren(new QName("http://52north.org/pubsub/amqp-10-delivery", "queue"),refParams);
            first = queueElem.findFirst();
            String queue;
            if (first.isPresent()) {
                queue = XmlBeansHelper.extractStringContent(first.get());
            }
            else {
                throw new SubscribeFailedException("No queue id in response");
            }

            Stream<XmlObject> consumerElem = XmlBeansHelper.findChildren(ConsumerReferenceDocument.type.getDocumentElementName(),refParams);
            first = consumerElem.findFirst();
            String consumerAddr;
            if (first.isPresent()) {
                EndpointReferenceType ref = (EndpointReferenceType) first.get();
                consumerAddr = ref.getAddress().getStringValue();
            }
            else {
                throw new SubscribeFailedException("No consumerAddr id in response");
            }


            return new SubscriptionProperties(deliveryMethod, subId, queue, consumerAddr);
        } catch (IOException | XmlException ex) {
            throw new SubscribeFailedException(ex.getMessage(), ex);
        }

    }


}
