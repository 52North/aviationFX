
package org.n52.aviation.aviationfx.model;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Airspace {

    private final Polygon polygon;
    private String type;
    private List<Position> outerRing;
    private String annotationNote;
    private double lowerLimit;
    private double upperLimit;
    private String altitudeUnit;
    private String identifier;

    public Airspace(Polygon polygon) {
        Objects.requireNonNull(polygon);
        this.polygon = polygon;

        Coordinate[] coords = this.polygon.getExteriorRing().getCoordinates();
        this.outerRing = new ArrayList<>(coords.length);
        for (int i = 0; i < coords.length; i++) {
            this.outerRing.add(new Position(coords[i].y, coords[i].x));
        }
    }

    public List<Position> getOuterRing() {
        return outerRing;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAnnotationNote() {
        return annotationNote;
    }

    public void setAnnotationNote(String annotationNote) {
        this.annotationNote = annotationNote;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
