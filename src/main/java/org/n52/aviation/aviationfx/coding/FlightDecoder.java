
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class FlightDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(FlightDecoder.class);

    public Flight decode(XmlObject xo) throws IOException {
        if (xo instanceof FlightDocument) {
           FlightDocument fd = (FlightDocument) xo;
           Position curr = parseCurrentPosition(fd);
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

        return 0.0;
    }


}
