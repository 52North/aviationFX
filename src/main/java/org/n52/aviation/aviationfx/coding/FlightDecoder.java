
package org.n52.aviation.aviationfx.coding;

import org.n52.aviation.aviationfx.model.Route;
import org.n52.aviation.aviationfx.model.Flight;
import org.n52.aviation.aviationfx.model.Position;
import aero.fixm.base.x30.SignificantPointType;
import aero.fixm.flight.x30.AircraftPositionType;
import aero.fixm.flight.x30.EnRouteType;
import aero.fixm.flight.x30.FlightDocument;
import aero.fixm.flight.x30.RouteSegmentType;
import aero.fixm.flight.x30.RouteType;
import aero.fixm.foundation.x30.GeographicLocationType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class FlightDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(FlightDecoder.class);
    private final GeometryFactory geometryFactory;
    private final Envelope tb12bbox;
    private final Envelope tb12bbox2;
    private final Envelope tb12bbox3;

    public FlightDecoder() {
        this.geometryFactory = new GeometryFactory();
        this.tb12bbox = new Envelope(new Coordinate(-88.991824, 36.594847), new Coordinate(-77.845203, 29.387093));
        this.tb12bbox2 = new Envelope(new Coordinate(-93.940118, 41.044241), new Coordinate(-88.178444, 37.142075));

        //tampa --> LA
        this.tb12bbox3 = new Envelope(new Coordinate(-84.051634, 28.954655), new Coordinate(-81.196927, 26.999078));
    }



    public Flight decode(XmlObject xo) throws IOException {
        if (xo instanceof FlightDocument) {
           FlightDocument fd = (FlightDocument) xo;
           Position curr = parseCurrentPosition(fd);

           curr = applyTestbed12Shift(curr);

           String gufi = parseGufi(fd);
           String identification = parseIdentification(fd);
           double bearing = parseBearing(fd);
           Route route = parseRoute(fd);
           return new Flight(gufi, identification, bearing, curr, route);
       }

       return null;
    }

    public Flight decode(InputStream inputStream) throws IOException {
        try {
            XmlObject xo = XmlObject.Factory.parse(inputStream);
            return decode(xo);
        } catch (XmlException ex) {
            throw new IOException(ex);
        }
    }

    private Position parseCurrentPosition(FlightDocument xo) {
        if (xo.getFlight().isSetEnRoute()) {
            EnRouteType enroute = xo.getFlight().getEnRoute();
            if (enroute.isSetPosition()) {
                AircraftPositionType pos = enroute.getPosition();
                if (pos.isSetPosition()) {
                    SignificantPointType pos2 = pos.getPosition();
                    return extractPosition(pos2);
                }
            }
        }
        return null;
    }

    private Position extractPosition(XmlObject pos2) throws NumberFormatException {
        XmlCursor cur = pos2.newCursor();
        if (cur.toFirstChild()) {
            XmlObject locObj = cur.getObject();
            if (locObj instanceof GeographicLocationType) {
                GeographicLocationType lpt = (GeographicLocationType) locObj;
                List posList = lpt.getPos();
                if (posList.size() == 2) {
                    return new Position(Double.parseDouble(posList.get(0).toString()),
                            Double.parseDouble(posList.get(1).toString()));
                }
            }
        }
        return null;
    }

    private String parseGufi(FlightDocument fd) {
        if (fd.getFlight().isSetGufi()) {
            return fd.getFlight().getGufi().getStringValue();
        }
        return null;
    }

    private String parseIdentification(FlightDocument fd) {
        if (fd.getFlight().isSetFlightIdentification()) {
            return fd.getFlight().getFlightIdentification().getAircraftIdentification();
        }
        return null;
    }

    private Route parseRoute(FlightDocument fd) {
        List<Position> result = new ArrayList<>();

        if (fd.getFlight().isSetAgreed() && fd.getFlight().getAgreed().isSetRoute()) {
            RouteType r = fd.getFlight().getAgreed().getRoute();
            RouteSegmentType[] segments = r.getSegmentArray();
            if (segments != null) {
                for (RouteSegmentType segment : segments) {
                    if (segment.isSetRoutePoint()) {
                        SignificantPointType point = segment.getRoutePoint().getPoint();
                        if (point != null) {
                            Position position = extractPosition(point);
                            if (position != null) {
                                result.add(position);
                            }
                        }
                    }
                }
            }
        }

        return new Route(result);
    }

    private double parseBearing(FlightDocument fd) {
        if (fd.getFlight().isSetEnRoute()) {
            EnRouteType en = fd.getFlight().getEnRoute();
            if (en.isSetPosition() && en.getPosition().isSetTrack()) {
                return en.getPosition().getTrack().getDoubleValue();
            }
        }

        LOG.warn("No Bearing for Doc: "+fd.xmlText(new XmlOptions().setSavePrettyPrint()));
        return 0.0;
    }

    private Position applyTestbed12Shift(Position curr) {
        Point geom = this.geometryFactory.createPoint(new Coordinate(curr.getLongitude(), curr.getLatitude()));
        if (tb12bbox.contains(geom.getCoordinate())) {
            return new Position(curr.getLatitude() + 4.158672 + 0.313166, curr.getLongitude() - 37.830098 + 0.091904 - 0.491788);
        }
        else if (tb12bbox2.contains(geom.getCoordinate())) {
            return new Position(curr.getLatitude() - 0.937475, curr.getLongitude() - 32.054741);
        }
        else if (tb12bbox3.contains(geom.getCoordinate())) {
            return new Position(curr.getLatitude() + 5.96084, curr.getLongitude() - 35.887528);
        }

        return curr;
    }

}
