
package org.n52.aviation.aviationfx.model;

import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class PubSubService {

    private String host;
    private boolean authenticated;
    private List<DeliveryMethod> deliveryMethods;
    private List<Publication> publications;
    private String details;

    public PubSubService() {
    }

    public PubSubService(String host, boolean authenticated, List<DeliveryMethod> deliveryMethods, List<Publication> publications, String details) {
        this.host = host;
        this.authenticated = authenticated;
        this.deliveryMethods = deliveryMethods;
        this.publications = publications;
        this.details = details;
    }

    public List<DeliveryMethod> getDeliveryMethods() {
        return deliveryMethods;
    }

    public void setDeliveryMethods(List<DeliveryMethod> deliveryMethods) {
        this.deliveryMethods = deliveryMethods;
    }

    public String getHost() {
        return host;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}
