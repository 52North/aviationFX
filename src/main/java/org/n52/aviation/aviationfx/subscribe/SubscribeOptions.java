
package org.n52.aviation.aviationfx.subscribe;

import org.n52.aviation.aviationfx.model.Polygon;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscribeOptions {

    private String host;
    private String pubId;
    private String deliveryMethod;
    private Polygon areaOfInterest;

    public SubscribeOptions() {
    }


    public SubscribeOptions(String host, String pubId, String deliveryMethod) {
        this(host, pubId, deliveryMethod, null);
    }

    public SubscribeOptions(String host, String pubId, String deliveryMethod, Polygon areaOfInterest) {
        this.host = host;
        this.pubId = pubId;
        this.deliveryMethod = deliveryMethod;
        this.areaOfInterest = areaOfInterest;
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

    public Polygon getAreaOfInterest() {
        return areaOfInterest;
    }


}
