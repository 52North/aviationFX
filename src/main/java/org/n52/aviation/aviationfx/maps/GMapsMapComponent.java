package org.n52.aviation.aviationfx.maps;

import aero.aixm.schema.x51.AirspaceDocument;
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
import com.lynden.gmapsfx.shapes.Rectangle;
import com.lynden.gmapsfx.shapes.RectangleOptions;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import netscape.javascript.JSObject;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.amqp.ContentType;
import org.n52.aviation.aviationfx.consume.NewMessageEvent;
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
        LatLong center = new LatLong(51.93472, 7.6499113);
        gmapView.addMapReadyListener(() -> {
            LOG.info("map Ready!");
        });

        MapOptions options = new MapOptions();
        options.center(center)
                .mapMarker(true)
                .zoom(11)
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

        LatLong centreC = new LatLong(47.545481, -121.87384);
        CircleOptions cOpts = new CircleOptions()
                .center(centreC)
                .radius(5000)
                .strokeColor("green")
                .strokeWeight(2)
                .fillColor("orange")
                .fillOpacity(0.3);

        Circle c = new Circle(cOpts);
        map.addMapShape(c);
        map.addUIEventHandler(c, UIEventType.click, (JSObject obj) -> {
            c.setEditable(!c.getEditable());
        });

        LatLongBounds llb = new LatLongBounds(new LatLong(47.533893, -122.89856), new LatLong(47.580694, -122.80312));
        RectangleOptions rOpts = new RectangleOptions()
                .bounds(llb)
                .strokeColor("black")
                .strokeWeight(2)
                .fillColor("null");

        Rectangle rt = new Rectangle(rOpts);
        map.addMapShape(rt);

        LatLong arcC = new LatLong(47.227029, -121.81641);
        double startBearing = 0;
        double endBearing = 30;
        double radius = 30000;

        MVCArray path = ArcBuilder.buildArcPoints(arcC, startBearing, endBearing, radius);
        path.push(arcC);

        Polygon arc = new Polygon(new PolygonOptions()
                .paths(path)
                .strokeColor("blue")
                .fillColor("lightBlue")
                .fillOpacity(0.3)
                .strokeWeight(2)
                .editable(false));

        map.addMapShape(arc);
        map.addUIEventHandler(arc, UIEventType.click, (JSObject obj) -> {
            arc.setEditable(!arc.getEditable());
        });

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

                if (xo instanceof AirspaceDocument) {
                    LOG.info("Got Airspace!");
                }
            } catch (XmlException ex) {
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

}
