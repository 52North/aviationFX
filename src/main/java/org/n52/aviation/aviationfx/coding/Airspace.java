
package org.n52.aviation.aviationfx.coding;

import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Airspace {

    private final Polygon polygon;

    public Airspace(Polygon polygon) {
        this.polygon = polygon;
    }

    public Polygon getPolygon() {
        return polygon;
    }

}
