package org.n52.aviation.aviationfx.consume;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.n52.amqp.AmqpConnectionCreationFailedException;
import org.n52.amqp.Connection;
import org.n52.amqp.ConnectionBuilder;
import org.n52.amqp.ContentType;
import org.n52.aviation.aviationfx.spring.Constructable;
import org.n52.aviation.aviationfx.subscribe.NewSubscriptionEvent;
import org.n52.aviation.aviationfx.subscribe.SubscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import rx.schedulers.Schedulers;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class AmqpConsumer implements Constructable {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpConsumer.class);

    private final Map<String, Connection> subscriptions = new HashMap<>();

    @Autowired
    private EventBus eventBus;

    @Override
    public void construct() {
        this.eventBus.register(this);
//        try {
//            createClient("amqp://ows.dev.52north.org/subverse.FIXM.bwxgqyukki");
//        } catch (AmqpCreationFailedException ex) {
//            LOG.warn(ex.getMessage(), ex);
//        }
    }

    @Subscribe
    public synchronized void onNewSubscription(NewSubscriptionEvent event) {
        if (event.getProperties().getDeliveryMethod().equals("https://docs.oasis-open.org/amqp/core/v1.0")) {
            try {
                this.subscriptions.put(event.getProperties().getId(), createClient(event.getProperties().getAddress(),
                        event.getProperties().getId()));
                LOG.info("New AMQP consumer: {}", event.getProperties().getAddress());
            } catch (AmqpCreationFailedException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }

    public void shutdown() {
        this.subscriptions.keySet().stream().forEach((string) -> {
            try {
                Connection receiver = this.subscriptions.get(string);
                receiver.close();
            } catch (RuntimeException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        });
    }

    private Connection createClient(String address, String subId) throws AmqpCreationFailedException {
        try {
            Connection conn = ConnectionBuilder.create(new URI(address)).build();

            conn.createObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(n -> {
                        if (n != null) {
                            LOG.debug("Received message: {}", n);
                            eventBus.post(new NewMessageEvent(n.getBody(),
                                    n.getContentType().orElse(ContentType.TEXT_PLAIN), subId));
                        }
                    });

            LOG.info("Subscribed to {}", address);

            return conn;
        } catch (URISyntaxException | AmqpConnectionCreationFailedException ex) {
            throw new AmqpCreationFailedException(ex);
        }
    }


}
