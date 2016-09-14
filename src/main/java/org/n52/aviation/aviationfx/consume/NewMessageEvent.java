package org.n52.aviation.aviationfx.consume;

import java.util.Optional;
import org.n52.amqp.ContentType;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class NewMessageEvent {

    private final Object message;
    private final ContentType contentType;
    private final String subscriptionId;

    public NewMessageEvent(Object value) {
        this(value, null);
    }

    public NewMessageEvent(Object value, ContentType ct) {
        this(value, ct, null);
    }

    public NewMessageEvent(Object value, ContentType ct, String subscriptionId) {
        this.message = value;
        this.contentType = ct;
        this.subscriptionId = subscriptionId;
    }

    public Object getMessage() {
        return message;
    }

    public Optional<ContentType> getContentType() {
        return Optional.ofNullable(contentType);
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

}
