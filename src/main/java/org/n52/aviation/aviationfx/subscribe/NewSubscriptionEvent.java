package org.n52.aviation.aviationfx.subscribe;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class NewSubscriptionEvent {

    private final SubscriptionProperties properties;

    public NewSubscriptionEvent(SubscriptionProperties properties) {
        this.properties = properties;
    }

    public SubscriptionProperties getProperties() {
        return properties;
    }

}
