
package org.n52.aviation.aviationfx.model;

import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Polygon {

    private List<Position> outerRing;

    public Polygon() {
        this.outerRing = null;
    }

    public void setOuterRing(List<Position> outerRing) {
        this.outerRing = outerRing;
    }

    public Polygon(List<Position> outerRing) {
        this.outerRing = outerRing;
    }

    public List<Position> getOuterRing() {
        return outerRing;
    }

}
