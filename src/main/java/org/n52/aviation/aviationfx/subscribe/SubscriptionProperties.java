package org.n52.aviation.aviationfx.subscribe;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class SubscriptionProperties {

    private final String deliveryMethod;
    private final String id;
    private final String endpointParameter;

    public SubscriptionProperties(String deliveryMethod, String id, String endpointParameter) {
        this.deliveryMethod = deliveryMethod;
        this.id = id;
        this.endpointParameter = endpointParameter;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getId() {
        return id;
    }

    public String getEndpointParameter() {
        return endpointParameter;
    }

}
