package org.n52.aviation.aviationfx.subscribe;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.namespace.QName;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.PublicationType;
import net.opengis.pubsub.x10.PublisherCapabilitiesDocument;
import net.opengis.pubsub.x10.SubscriptionIdentifierDocument;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.aviation.aviationfx.XmlBeansHelper;
import org.n52.aviation.aviationfx.coding.SubscribeEncoder;
import org.n52.aviation.aviationfx.model.DeliveryMethod;
import org.n52.aviation.aviationfx.model.Publication;
import org.oasisOpen.docs.wsn.b2.ConsumerReferenceDocument;
import org.oasisOpen.docs.wsn.b2.SubscribeResponseDocument.SubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2005.x08.addressing.EndpointReferenceType;
import org.w3.x2005.x08.addressing.ReferenceParametersType;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class SubscribeController {

    private static final Logger LOG = LoggerFactory.getLogger(SubscribeController.class);

    private Map<String, Publication> publications = new HashMap<>();
    private Map<String, DeliveryMethod> deliveryMethods = new HashMap<>();
    private String amqpDefaultBroker;
    private final EventBus eventBus;
    private final String host;
    private org.n52.aviation.aviationfx.model.Credentials credentials;
    private final URI hostUri;

    public SubscribeController(String serverUrl, EventBus eb) {
        this.host = serverUrl;
        this.hostUri = URI.create(serverUrl);
        this.eventBus = eb;
    }

    public void setCredentials(org.n52.aviation.aviationfx.model.Credentials credentials) {
        this.credentials = credentials;
    }

    public void requestServiceCapabilities() {
        String serverUrl = host.concat("?service=PubSub&request=GetCapabilities");

        HttpGet get = new HttpGet(serverUrl);
        CredentialsProvider credsProvider = checkAndSetAuthentication();

        LOG.info("Requesting capabilities: "+serverUrl);
        try (CloseableHttpClient c = HttpClientBuilder.create().setDefaultCredentialsProvider(credsProvider).build()) {
            CloseableHttpResponse resp = c.execute(get);
            PublisherCapabilitiesDocument pubCapDoc = PublisherCapabilitiesDocument.Factory.parse(resp.getEntity().getContent());

            //update UI
            updatePublications(pubCapDoc);
            updateDeliveryMethods(pubCapDoc);

        } catch (IOException | XmlException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    private CredentialsProvider checkAndSetAuthentication() {
        if (this.credentials != null && this.credentials.getUser() != null && this.credentials.getPassword() != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(hostUri.getHost(), hostUri.getPort()),
                new UsernamePasswordCredentials(this.credentials.getUser(), this.credentials.getPassword()));

            LOG.info("returning credentials provider: "+credentialsProvider);
            return credentialsProvider;
        }

        LOG.info("No credentials set");
        return null;
    }

    private void updateDeliveryMethods(PublisherCapabilitiesDocument pubCapDoc) {
        DeliveryMethodType[] deliveryArray = pubCapDoc.getPublisherCapabilities().getDeliveryCapabilities().getDeliveryMethodArray();
        if (deliveryArray != null) {
            for (DeliveryMethodType dm : deliveryArray) {
                String abstr = dm.getAbstractArray(0).getStringValue();
                String id = dm.getIdentifier();
                deliveryMethods.put(id, new DeliveryMethod(id, abstr));

                //TODO: implement cleaner way
                if (dm.getExtensionArray() != null) {
                    for (XmlObject ext : dm.getExtensionArray()) {
                        Optional<XmlObject> child = XmlBeansHelper.findFirstChild(new QName("http://www.opengis.net/pubsub/1.0/amqp/v1.0", "defaultHost"), ext);
                        if (child.isPresent()) {
                            this.amqpDefaultBroker = XmlBeansHelper.extractStringContent(child.get());
                        }
                    }
                }
            }

        }
    }

    private void updatePublications(PublisherCapabilitiesDocument pubCapDoc) {
        PublicationType[] pubArray = pubCapDoc.getPublisherCapabilities().getPublications().getPublicationArray();
        if (pubArray != null) {
            for (PublicationType p : pubArray) {
                String id = p.getIdentifier();
                String abstr = p.getAbstractArray(0).getStringValue();
                publications.put(id, new Publication(id, abstr));
            }

        }
    }

    public SubscriptionProperties subscribe(SubscribeOptions options) throws SubscribeFailedException {
        EnvelopeDocument envDoc = new SubscribeEncoder().encodeSubscription(options, amqpDefaultBroker);
        HttpPost post = new HttpPost(host);
        CredentialsProvider credProv = checkAndSetAuthentication();

        try (CloseableHttpClient c = HttpClientBuilder.create().setDefaultCredentialsProvider(credProv).build()) {
            String xml = envDoc.xmlText(new XmlOptions().setSavePrettyPrint());
            LOG.info("Subscribe Request: "+xml);
            post.setEntity(new StringEntity(xml));
            post.setHeader("Content-Type", "application/soap+xml");

            CloseableHttpResponse resp = c.execute(post);
            XmlObject xo = XmlObject.Factory.parse(resp.getEntity().getContent());
            if (!(xo instanceof EnvelopeDocument)) {
                LOG.warn("Could not process response: "+xo.xmlText());
                throw new SubscribeFailedException("Could not process response: "+xo.xmlText());
            }

            envDoc = (EnvelopeDocument) xo;
            XmlCursor cur = envDoc.getEnvelope().getBody().newCursor();
            cur.toFirstChild();
            XmlObject respBody = cur.getObject();

            if (!(respBody instanceof SubscribeResponse)) {
                LOG.warn("Not a valid subscribe response: "+xo.xmlText());
                throw new SubscribeFailedException("Not a valid subscribe response: "+xo.xmlText());
            }

            SubscribeResponse subRespDoc = (SubscribeResponse) respBody;

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

            SubscriptionProperties subProps = new SubscriptionProperties(options.getDeliveryMethod(), subId, consumerAddr, host);
            if (credProv != null) {
                Credentials creds = credProv.getCredentials(new AuthScope(post.getURI().getHost(), post.getURI().getPort()));
                subProps.setAuthentication(new Authentication(creds.getUserPrincipal().getName(), creds.getPassword()));
            }

            new Thread(() -> {
                eventBus.post(new NewSubscriptionEvent(subProps));
            }).start();

            return subProps;
        } catch (IOException | XmlException ex) {
            throw new SubscribeFailedException(ex.getMessage(), ex);
        }

    }

    public Map<String, DeliveryMethod> getDeliveryMethods() {
        return deliveryMethods;
    }

    public Map<String, Publication> getPublications() {
        return publications;
    }

}
