package org.n52.aviation.aviationfx.subscribe;

import com.google.common.base.MoreObjects;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class SubscriptionProperties {

    private final String deliveryMethod;
    private final String id;
    private final String address;

    public SubscriptionProperties(String deliveryMethod, String id, String address) {
        this.deliveryMethod = deliveryMethod;
        this.id = id;
        this.address = address;
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


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("deliveryMethod", deliveryMethod)
                .add("address", address).toString();
    }



}
