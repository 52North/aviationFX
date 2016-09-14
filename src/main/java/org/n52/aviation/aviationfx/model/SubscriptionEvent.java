
package org.n52.aviation.aviationfx.model;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscriptionEvent {

    private final String subscriptionId;
    private final Object message;

    public SubscriptionEvent(String subscriptionId, Object message) {
        this.subscriptionId = subscriptionId;
        this.message = message;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public Object getMessage() {
        return message;
    }

}
