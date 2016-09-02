
package org.n52.aviation.aviationfx.model;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Publication {

    private String identifier;
    private String abstrakt;

    public Publication() {
    }

    public Publication(String identifier, String abstrakt) {
        this.identifier = identifier;
        this.abstrakt = abstrakt;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getAbstrakt() {
        return abstrakt;
    }

}
