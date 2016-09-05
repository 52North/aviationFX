package org.n52.aviation.aviationfx.subscribe;

import com.google.common.base.MoreObjects;
import org.joda.time.DateTime;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class SubscriptionProperties {

    private final String deliveryMethod;
    private final String id;
    private final String address;
    private final String pubSubHost;
    private transient Authentication authentication;
    private DateTime endOfLife;

    public SubscriptionProperties(String deliveryMethod, String id, String address, String pubSubHost) {
        this.deliveryMethod = deliveryMethod;
        this.id = id;
        this.address = address;
        this.pubSubHost = pubSubHost;
    }

    public String getAddress() {
        return address;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getId() {
        return id;
    }

    public String getPubSubHost() {
        return pubSubHost;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public DateTime getEndOfLife() {
        return endOfLife;
    }

    public void setEndOfLife(DateTime endOfLife) {
        this.endOfLife = endOfLife;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("deliveryMethod", deliveryMethod)
                .add("address", address)
                .add("pubSubHost", pubSubHost)
                .toString();
    }

    public Authentication getAuthentication() {
        return this.authentication;
    }



}
