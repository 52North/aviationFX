package org.n52.aviation.aviationfx;

import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import net.opengis.pubsub.x10.SubscriptionIdentifierDocument;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.aviation.aviationfx.subscribe.NewSubscriptionEvent;
import org.n52.aviation.aviationfx.subscribe.SubscriptionProperties;
import org.oasisOpen.docs.wsn.b2.UnsubscribeDocument;
import org.oasisOpen.docs.wsn.b2.UnsubscribeResponseDocument.UnsubscribeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

public class MainController {

    private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

    private final Map<String, SubscriptionProperties> subscriptions = new HashMap<>();

    @Subscribe
    public void onNewSubscription(NewSubscriptionEvent event) {
        String id = event.getProperties().getId();
        this.subscriptions.put(id, event.getProperties());
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

        HttpPost post = new HttpPost(props.getPubSubHost());

        CredentialsProvider credProv = checkCredentialsProvider(post, props);

        try (CloseableHttpClient c = HttpClientBuilder.create().setDefaultCredentialsProvider(credProv).build()) {

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

        } catch (IOException | XmlException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private CredentialsProvider checkCredentialsProvider(HttpPost hrb, SubscriptionProperties props) {
        if (props.getAuthentication() != null) {
            String user = props.getAuthentication().getUser();
            String pw = props.getAuthentication().getPassword();

            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(new AuthScope(hrb.getURI().getHost(), hrb.getURI().getPort()),
                    new UsernamePasswordCredentials(user, pw));

            return credentialsProvider;
        }

        return null;
    }

}
