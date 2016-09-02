
package org.n52.aviation.aviationfx.model;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class DeliveryMethod {

    private String identifier;
    private String abstrakt;

    public DeliveryMethod() {
    }

    public DeliveryMethod(String identifier, String abstrakt) {
        this.identifier = identifier;
        this.abstrakt = abstrakt;
    }

    public String getAbstrakt() {
        return abstrakt;
    }

    public String getIdentifier() {
        return identifier;
    }

}
