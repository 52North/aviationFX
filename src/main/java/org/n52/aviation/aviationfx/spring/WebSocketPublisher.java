
package org.n52.aviation.aviationfx.spring;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.n52.aviation.aviationfx.model.Airspace;
import org.n52.aviation.aviationfx.model.Flight;
import org.n52.aviation.aviationfx.model.Position;
import org.n52.aviation.aviationfx.model.Route;
import org.n52.aviation.aviationfx.model.SubscriptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@Configuration
@EnableWebSocket
public class WebSocketPublisher implements WebSocketConfigurer, Constructable, Destroyable {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketPublisher.class);
    private final Set<WebSocketSession> activeSessions = new HashSet<>();

    @Autowired
    private EventBus eventBus;

    @Autowired
    private CustomObjectMapper mapper;
    private Thread sender;
    private boolean running = true;
    private List<SubscriptionEvent> buffer = new ArrayList<>();

    @Override
    public void construct() {
        this.eventBus.register(this);
        new Thread(() -> {
            int delta = 0;
            while (true) {
                delta++;
                Flight f = new Flight("tester", "1233asd123swde", 123, new Position(52+delta*0.01, 1+delta*0.01),
                        new Route(Arrays.asList(new Position[] {
                            new Position(51, 7),
                            new Position(52, 7.5),
                            new Position(52.5, 7.6)
                })));
                convertAndSend(f);

                GeometryFactory gf = new GeometryFactory();
                Airspace a = new Airspace(gf.createPolygon(gf.createLinearRing(new Coordinate[] {
                    new Coordinate(6, 50),
                    new Coordinate(6, 54),
                    new Coordinate(8, 54),
                    new Coordinate(8, 50),
                    new Coordinate(6, 50)
                }), null));
                a.setType("P");
                a.setIdentifier("test-test-123");
                a.setAnnotationNote("SAR Activity due to forest fires");
                convertAndSend(a);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }

        });

        this.sender = new Thread(() -> {
            while (running) {
                synchronized (WebSocketPublisher.this) {
                    while (buffer.isEmpty()) {
                        try {
                            WebSocketPublisher.this.wait();
                        } catch (InterruptedException ex) {
                            LOG.warn(ex.getMessage(), ex);
                        }
                    }

                    convertAndSend(buffer);
                    buffer.clear();
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }
        });
        this.sender.start();

    }

    @Subscribe
    public void onSubscriptionEvent(SubscriptionEvent se) {
        synchronized (this) {
            this.buffer.add(se);
            this.notifyAll();
        }
    }

    @Override
    public void destroy() {
        this.eventBus.unregister(this);
        this.running = false;
    }


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(asyncJobHandler(), "/api/messages").withSockJS();
    }

    public WebSocketHandler asyncJobHandler() {
        return new AbstractWebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
                synchronized (WebSocketPublisher.this) {
                    activeSessions.add(session);
                }
                LOG.info("New WebsocketSession: {}", session.getRemoteAddress());
            }


            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                synchronized (WebSocketPublisher.this) {
                    activeSessions.remove(session);
                }
                LOG.info("WebsocketSession closed: {}; {}", session.getRemoteAddress(), closeStatus);
            }

            @Override
            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
                LOG.warn("Transport error on websocket '{}': {}", session.getRemoteAddress(), exception.getMessage());
                LOG.debug(exception.getMessage(), exception);
            }
        };
    }

    private void convertAndSend(Object mappable) {
        TextMessage msg;
        try {
            msg = new TextMessage(mapper.writeValueAsString(mappable));
        } catch (IOException ex) {
            LOG.warn("Could not send websocket message: {}", ex.getMessage());
            LOG.debug(ex.getMessage(), ex);
            return;
        }
        synchronized(this) {
            activeSessions.stream().forEach(s -> {
                try {
                    s.sendMessage(msg);
                } catch (IOException ex) {
                    LOG.warn("Could not send websocket message: {}", ex.getMessage());
                    LOG.debug(ex.getMessage(), ex);
                }
            });
        }
    }
}
