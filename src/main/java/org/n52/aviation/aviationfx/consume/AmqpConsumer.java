package org.n52.aviation.aviationfx.consume;

import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.apache.activemq.transport.amqp.client.AmqpClient;
import org.apache.activemq.transport.amqp.client.AmqpConnection;
import org.apache.activemq.transport.amqp.client.AmqpMessage;
import org.apache.activemq.transport.amqp.client.AmqpReceiver;
import org.n52.aviation.aviationfx.subscribe.NewSubscriptionEvent;
import org.n52.aviation.aviationfx.subscribe.SubscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class AmqpConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(AmqpConsumer.class);

    private final Map<String, AmqpReceiver> subscriptions = new HashMap<>();
    private boolean running = true;

    @Subscribe
    public synchronized void onNewSubscription(NewSubscriptionEvent event) {
        if (event.getProperties().getDeliveryMethod().equals("amqp10")) {
            try {
                this.subscriptions.put(event.getProperties().getId(), createClient(event.getProperties()));
            } catch (AmqpConnectionCreationFailedException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }

    public void shutdown() {
        this.running = false;

        for (String string : this.subscriptions.keySet()) {
            try {
                this.subscriptions.get(string).close();
            } catch (IOException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
    }

    private AmqpReceiver createClient(SubscriptionProperties properties) throws AmqpConnectionCreationFailedException {
        try {
            AmqpClient client = new AmqpClient(new URI(properties.getAddress()), null, null);
            AmqpConnection conn = client.connect();

            AmqpReceiver receiver = conn.createSession().createReceiver(properties.getEndpointParameter());

            spawnReceiverThread(receiver, properties.getId());

            return receiver;
        } catch (Exception ex) {
            throw new AmqpConnectionCreationFailedException(ex);
        }
    }

    private void spawnReceiverThread(AmqpReceiver receiver, String id) {
        new Thread(() -> {
            synchronized (AmqpConsumer.this) {
                while (running) {
                    if (!subscriptions.keySet().contains(id)) {
                        return;
                    }
                }

                try {
                    AmqpMessage msg = receiver.receive();
                    LOG.info("Received message: %s", msg);
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }
        }).start();
    }

}
