package org.n52.aviation.aviationfx.maps;

import aero.aixm.schema.x51.AirspaceDocument;
import aero.aixm.schema.x51.message.AIXMBasicMessageDocument;
import aero.fixm.flight.x30.FlightDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.LatLongBounds;
import com.lynden.gmapsfx.javascript.object.MVCArray;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.shapes.ArcBuilder;
import com.lynden.gmapsfx.shapes.Circle;
import com.lynden.gmapsfx.shapes.CircleOptions;
import com.lynden.gmapsfx.shapes.Polygon;
import com.lynden.gmapsfx.shapes.PolygonOptions;
import com.lynden.gmapsfx.shapes.Polyline;
import com.lynden.gmapsfx.shapes.PolylineOptions;
import com.lynden.gmapsfx.shapes.Rectangle;
import com.lynden.gmapsfx.shapes.RectangleOptions;
import com.vividsolutions.jts.geom.Coordinate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import netscape.javascript.JSObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.amqp.ContentType;
import org.n52.aviation.aviationfx.coding.Airspace;
import org.n52.aviation.aviationfx.coding.AirspaceDecoder;
import org.n52.aviation.aviationfx.coding.FlightDecoder;
import org.n52.aviation.aviationfx.consume.NewMessageEvent;
import org.n52.aviation.aviationfx.model.Flight;
import org.n52.aviation.aviationfx.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class GMapsMapComponent implements MapComponent, MapComponentInitializedListener {

    private static final Logger LOG = LoggerFactory.getLogger(GMapsMapComponent.class);
    private final GoogleMapView gmapView;
    private GoogleMap map;
    private String windowBaseLocation = "";
    private final Map<String, Marker> aircraftMarkers = new HashMap<>();
    private final Map<String, List<Flight>> flightLog = new HashMap<>();
    private InfoWindow openedWindow;
    private Polyline routePolyLine;
    private Polyline historyPolyLine;
    private String activeGufi;

    public GMapsMapComponent(GoogleMapView gmapView) {
        this.gmapView = gmapView;
    }

    @Override
    public void initialize() {
        gmapView.addMapInializedListener(this);
    }

    @Override
    public void mapInitialized() {
        //Once the map has been loaded by the Webview, initialize the map details.
        LatLong center = new LatLong(34.90760087214065, -119.5447705078125);
        gmapView.addMapReadyListener(() -> {
            LOG.info("map Ready!");
        });

        MapOptions options = new MapOptions();
        options.center(center)
                .mapMarker(true)
                .zoom(8)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(true)
                .mapType(MapTypeIdEnum.TERRAIN);

        map = gmapView.createMap(options);

        map.setHeading(123.2);


//        InfoWindowOptions infoOptions = new InfoWindowOptions();
//        infoOptions.content("<h2>Here's an info window</h2><h3>with some info</h3>")
//                .position(center);
//
//        InfoWindow window = new InfoWindow(infoOptions);
//        window.open(map, myMarker);

        LatLong poly1 = new LatLong(47.429945, -122.84363);
        LatLong poly2 = new LatLong(47.361153, -123.03040);
        LatLong poly3 = new LatLong(47.387193, -123.11554);
        LatLong poly4 = new LatLong(47.585789, -122.96722);
        LatLong[] pAry = new LatLong[]{poly1, poly2, poly3, poly4};
        MVCArray pmvc = new MVCArray(pAry);

        PolygonOptions polygOpts = new PolygonOptions()
                .paths(pmvc)
                .strokeColor("blue")
                .strokeWeight(2)
                .editable(false)
                .fillColor("lightBlue")
                .fillOpacity(0.5);

        Polygon pg = new Polygon(polygOpts);
        map.addMapShape(pg);
        map.addUIEventHandler(pg, UIEventType.click, (JSObject obj) -> {
            //polygOpts.editable(true);
            pg.setEditable(!pg.getEditable());
        });

//        LatLong centreC = new LatLong(47.545481, -121.87384);
//        CircleOptions cOpts = new CircleOptions()
//                .center(centreC)
//                .radius(5000)
//                .strokeColor("green")
//                .strokeWeight(2)
//                .fillColor("orange")
//                .fillOpacity(0.3);
//
//        Circle c = new Circle(cOpts);
//        map.addMapShape(c);
//        map.addUIEventHandler(c, UIEventType.click, (JSObject obj) -> {
//            c.setEditable(!c.getEditable());
//        });
//
//        LatLongBounds llb = new LatLongBounds(new LatLong(47.533893, -122.89856), new LatLong(47.580694, -122.80312));
//        RectangleOptions rOpts = new RectangleOptions()
//                .bounds(llb)
//                .strokeColor("black")
//                .strokeWeight(2)
//                .fillColor("null");
//
//        Rectangle rt = new Rectangle(rOpts);
//        map.addMapShape(rt);

//        LatLong arcC = new LatLong(47.227029, -121.81641);
//        double startBearing = 0;
//        double endBearing = 30;
//        double radius = 30000;

//        MVCArray path = ArcBuilder.buildArcPoints(arcC, startBearing, endBearing, radius);
//        path.push(arcC);

//        Polygon arc = new Polygon(new PolygonOptions()
//                .paths(path)
//                .strokeColor("blue")
//                .fillColor("lightBlue")
//                .fillOpacity(0.3)
//                .strokeWeight(2)
//                .editable(false));
//
//        map.addMapShape(arc);
//        map.addUIEventHandler(arc, UIEventType.click, (JSObject obj) -> {
//            arc.setEditable(!arc.getEditable());
//        });

        map.addUIEventHandler(UIEventType.click, h -> {
            Object result = h.eval("window.location");
            LOG.warn("window.location="+result);
        });

        String result = map.getJSObject().eval("window.location").toString();
        this.windowBaseLocation = result.substring(0, result.lastIndexOf("/"));
        LOG.info("window base location: "+windowBaseLocation);

    }

    @Override
    public void onNewMessage(NewMessageEvent event) {
        ContentType ct = event.getContentType().orElse(new ContentType("application/xml"));

        if (ct.getName().equals("application/xml")) {
            try {
                XmlObject xo = XmlObject.Factory.parse(event.getMessage().toString());

                if (xo instanceof AIXMBasicMessageDocument) {
                    LOG.info("Got AIXM");
                    Airspace airspace = new AirspaceDecoder().decode(xo);
                    onNewAirspace(airspace);
                }
                else if (xo instanceof FlightDocument) {
                    LOG.info("Got FIXM!");
                    Flight flight = new FlightDecoder().decode(xo);
                    onNewFlight(flight);
                }
            } catch (XmlException |IOException ex) {
                LOG.warn("Could not parse message", ex);
            }
        }
        else if (ct.getName().equals("application/json")) {
            LOG.info("New JSON data: "+event.getMessage());
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode json = mapper.readTree(event.getMessage().toString());
                if (json.has("hex") && json.has("lat")) {
                    //new ADS-B json
                    final String hex = json.get("hex").asText();
                    double lat = json.get("lat").asDouble();
                    double lon = json.get("lon").asDouble();
                    int heading = json.has("track") ? json.get("track").asInt() : 0;

                    Platform.runLater(() -> {
                        Marker m = null;
                        LatLong markerLatLong = new LatLong(lat, lon);

                        synchronized (GMapsMapComponent.this) {
                            if (aircraftMarkers.containsKey(hex)) {
                                m = aircraftMarkers.get(hex);
                                m.setPosition(markerLatLong);
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(markerLatLong)
                                    .title(json.get("hex").asText())
                                    .icon(windowBaseLocation+"/markers/aircraft_red_"+determineDirection(heading)+".png")
                                    .visible(true);
                                m.setOptions(markerOptions);
                                LOG.info("marker updated!");
                                if (isWithinBounds(markerLatLong, map.getBounds())) {
                                    map.setZoom(map.getZoom()-1);
                                    map.setZoom(map.getZoom()+1);
                                }
                            }
                        }

                        if (m == null) {
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(markerLatLong)
                                    .title(json.get("hex").asText())
                                    .icon(windowBaseLocation+"/markers/aircraft_red"+determineDirection(heading)+".png")
                                    .visible(true);
                            m = new Marker(markerOptions);
                            map.addMarker(m);

                            //popup
                            InfoWindowOptions infoOptions = new InfoWindowOptions();
                            infoOptions.content("<h3>"+hex+"</h3>");

                            InfoWindow window = new InfoWindow(infoOptions);

                            map.addUIEventHandler(m, UIEventType.click, (JSObject obj) -> {
                                window.open(map, aircraftMarkers.get(hex));
                            });

                            synchronized (GMapsMapComponent.this) {
                                aircraftMarkers.put(hex, m);
                            }
                            LOG.info("marker added!");
                        }

                    });
                }
            } catch (IOException | RuntimeException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }

    }

    private boolean isWithinBounds(LatLong p, LatLongBounds bounds) {
        LatLong ne = bounds.getNorthEast();
        LatLong sw = bounds.getSouthWest();

        return p.getLatitude() < ne.getLatitude() && p.getLongitude() < ne.getLongitude()
                && p.getLatitude() > sw.getLatitude() && p.getLongitude() > sw.getLongitude();
    }

    private String determineDirection(int heading) {
        int delta = 22;
        if (heading >= 315+delta) {
            return "n";
        }
        if (heading >= 270+delta) {
            return "nw";
        }
        if (heading >= 225+delta) {
            return "w";
        }
        if (heading >= 180+delta) {
            return "sw";
        }
        if (heading >= 135+delta) {
            return "s";
        }
        if (heading >= 90+delta) {
            return "se";
        }
        if (heading >= 45+delta) {
            return "e";
        }
        return "ne";
    }

    private void onNewFlight(Flight flight) {
        List<Flight> history;
        synchronized (this) {
            if (flightLog.containsKey(flight.getGufi())) {
                history = flightLog.get(flight.getGufi());
            }
            else {
                history = new ArrayList<>();
                this.flightLog.put(flight.getGufi(), history);
            }
        }

        history.add(flight);

        Platform.runLater(() -> {
            String gufi = flight.getGufi();
            int heading = (int) flight.getBearing();
            Marker m = null;
            LatLong markerLatLong = new LatLong(flight.getCurrentPosition().getLatitude(),
                    flight.getCurrentPosition().getLongitude());

            synchronized (GMapsMapComponent.this) {
                if (aircraftMarkers.containsKey(gufi)) {
                    m = aircraftMarkers.get(gufi);
                    m.setPosition(markerLatLong);
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(markerLatLong)
                        .title(gufi)
                        .icon(windowBaseLocation+"/markers/aircraft_red_"+determineDirection(heading)+".png")
                        .visible(true);
                    m.setOptions(markerOptions);

                    if (gufi.equals(this.activeGufi)) {
                        showRouteAndHistory(gufi);
                    }

                    LOG.info("marker updated!");
                    if (isWithinBounds(markerLatLong, map.getBounds())) {
                        map.setZoom(map.getZoom()-1);
                        map.setZoom(map.getZoom()+1);
                    }
                }
            }

            if (m == null) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(markerLatLong)
                        .title(gufi)
                        .icon(windowBaseLocation+"/markers/aircraft_red"+determineDirection(heading)+".png")
                        .visible(true);
                m = new Marker(markerOptions);
                map.addMarker(m);


                //popup
                InfoWindowOptions infoOptions = new InfoWindowOptions();
                infoOptions.content("<h3>"+gufi+"</h3>");

                InfoWindow window = new InfoWindow(infoOptions);

                map.addUIEventHandler(m, UIEventType.click, (JSObject obj) -> {
                    LOG.info("AIRCRAFT CLICKED!");

                    if (GMapsMapComponent.this.openedWindow != null) {
                        GMapsMapComponent.this.openedWindow.close();
                        removeRouteAndHistory();
                        if (GMapsMapComponent.this.openedWindow == window) {
                            GMapsMapComponent.this.openedWindow = null;
                            GMapsMapComponent.this.activeGufi = null;
                            return;
                        }
                    }
                    window.open(map, aircraftMarkers.get(gufi));
                    GMapsMapComponent.this.openedWindow = window;
                    showRouteAndHistory(gufi);
                    GMapsMapComponent.this.activeGufi = gufi;
                });

                map.addUIEventHandler(window, UIEventType.click, (JSObject obj) -> {
                    LOG.info("AIRCRAFT WINDOW CLICKED!");
                });


                synchronized (GMapsMapComponent.this) {
                    aircraftMarkers.put(gufi, m);
                }
                LOG.info("marker added!");
            }

        });
    }

    private void onNewAirspace(Airspace airspace) {
        Platform.runLater(() -> {
            List<LatLong> coordList = new ArrayList<>();

            for (Coordinate coordinate : airspace.getPolygon().getCoordinates()) {
                coordList.add(new LatLong(coordinate.y, coordinate.x));
            }
            LatLong[] coordArr = new LatLong[coordList.size()];
            MVCArray pmvc = new MVCArray(coordList.toArray(coordArr));

            PolygonOptions polygOpts = new PolygonOptions()
                    .paths(pmvc)
                    .strokeColor("blue")
                    .strokeWeight(2)
                    .editable(false)
                    .fillColor("lightBlue")
                    .fillOpacity(0.2);

            Polygon pg = new Polygon(polygOpts);
            map.addMapShape(pg);

            map.addUIEventHandler(pg, UIEventType.click, (JSObject obj) -> {
                LOG.info("AIRSPACE CLICKED!!");
            });
        });

    }

    private void removeRouteAndHistory() {
        if (this.routePolyLine != null) {
            map.removeMapShape(this.routePolyLine);
            this.routePolyLine = null;
        }
        if (this.historyPolyLine != null) {
            map.removeMapShape(this.historyPolyLine);
            this.historyPolyLine = null;
        }
    }

    private void showRouteAndHistory(String gufi) {
        removeRouteAndHistory();
        List<Flight> history = this.flightLog.get(gufi);

        PolylineOptions opts = new PolylineOptions().clickable(false);
        opts.clickable(false)
                .strokeColor("red")
                .strokeWeight(1.5);
        this.routePolyLine = new Polyline(opts);
        LatLong[] coordArr = new LatLong[history.size()];
        int index = 0;
        for (Flight flight : history) {
            Position pos = flight.getCurrentPosition();
            coordArr[index++] = new LatLong(pos.getLatitude(), pos.getLongitude());
        }
        MVCArray path = new MVCArray(coordArr);
        this.routePolyLine.setPath(path);
        map.addMapShape(this.routePolyLine);

        PolylineOptions opts2 = new PolylineOptions().clickable(false);

        List<Position> posList = history.get(0).getRoute().getPositionList();
        LatLong[] coordArr2 = new LatLong[posList.size()];
        index = 0;
        for (Position pos : posList) {
            coordArr2[index++] = new LatLong(pos.getLatitude(), pos.getLongitude());
        }
        MVCArray path2 = new MVCArray(coordArr2);
        opts2.path(path2)
                .clickable(false)
                .strokeColor("green")
                .strokeWeight(10.0)
                .strokeOpacity(0.4);
        this.historyPolyLine = new Polyline(opts2);

        map.addMapShape(this.historyPolyLine);
    }

}
