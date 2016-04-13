package org.n52.aviation.aviationfx.consume;

import com.google.common.eventbus.Subscribe;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.n52.amqp.AmqpConnectionCreationFailedException;
import org.n52.amqp.Connection;
import org.n52.amqp.ConnectionBuilder;
import org.n52.amqp.ContentType;
import org.n52.aviation.aviationfx.EventBusInstance;
import org.n52.aviation.aviationfx.subscribe.NewSubscriptionEvent;
import org.n52.aviation.aviationfx.subscribe.SubscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.schedulers.Schedulers;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class AmqpConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpConsumer.class);

    private final Map<String, Connection> subscriptions = new HashMap<>();
    private boolean running = true;

    @Subscribe
    public synchronized void onNewSubscription(NewSubscriptionEvent event) {
        if (event.getProperties().getDeliveryMethod().equals("https://docs.oasis-open.org/amqp/core/v1.0")) {
            try {
                this.subscriptions.put(event.getProperties().getId(), createClient(event.getProperties()));
                LOG.info("New AMQP consumer: {}", event.getProperties());
            } catch (AmqpCreationFailedException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }

    public void shutdown() {
        this.running = false;

        this.subscriptions.keySet().stream().forEach((string) -> {
            try {
                Connection receiver = this.subscriptions.get(string);
                receiver.close();
            } catch (RuntimeException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        });
    }

    private Connection createClient(SubscriptionProperties properties) throws AmqpCreationFailedException {
        try {
            Connection conn = ConnectionBuilder.create(new URI(properties.getAddress())).build();

            conn.createObservable()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.computation())
                    .subscribe(n -> {
                        if (n != null) {
                            LOG.info("Received message: {}", n);
                            EventBusInstance.getEventBus().post(new NewMessageEvent(n.getBody(),
                                    n.getContentType().orElse(ContentType.TEXT_PLAIN)));
                        }
                    });

            LOG.info("Subscribed to {}", properties.getAddress());

            return conn;
        } catch (URISyntaxException | AmqpConnectionCreationFailedException ex) {
            throw new AmqpCreationFailedException(ex);
        }
    }


}
