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

    public NewMessageEvent(Object value) {
        this(value, null);
    }

    public NewMessageEvent(Object value, ContentType ct) {
        this.message = value;
        this.contentType = ct;
    }

    public Object getMessage() {
        return message;
    }

    public Optional<ContentType> getContentType() {
        return Optional.ofNullable(contentType);
    }


}
