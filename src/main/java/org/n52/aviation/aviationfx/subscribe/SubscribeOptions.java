
package org.n52.aviation.aviationfx.subscribe;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscribeOptions {

    private String host;
    private String pubId;
    private String deliveryMethod;

    public SubscribeOptions() {
    }


    public SubscribeOptions(String host, String pubId, String deliveryMethod) {
        this.host = host;
        this.pubId = pubId;
        this.deliveryMethod = deliveryMethod;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public String getHost() {
        return host;
    }

    public String getPubId() {
        return pubId;
    }


}
