
package org.n52.aviation.aviationfx.coding;

import aero.aixm.schema.x51.message.AIXMBasicMessageDocument;
import aero.fixm.flight.x30.FlightDocument;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import java.io.IOException;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.aviation.aviationfx.consume.NewMessageEvent;
import org.n52.aviation.aviationfx.model.SubscriptionEvent;
import org.n52.aviation.aviationfx.spring.Constructable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class XmlBeansDecoder implements Constructable {

    private static final Logger LOG = LoggerFactory.getLogger(XmlBeansDecoder.class);

    @Autowired
    private EventBus eventBus;

    @Autowired
    private FlightDecoder flightDecoder;

    @Autowired
    private AirspaceDecoder airspaceDecoder;

    @Override
    public void construct() {
        this.eventBus.register(this);
    }

    @Subscribe
    public void newMessage(NewMessageEvent ev) {
        if (ev.getContentType().isPresent() && ev.getContentType().get().getName().equals("application/xml")) {
            try {
                XmlObject xo = XmlObject.Factory.parse(ev.getMessage().toString());
                Object o;

                if (xo instanceof FlightDocument) {
                    o = flightDecoder.decode(xo);
                }
                else if (xo instanceof AIXMBasicMessageDocument) {
                    o = airspaceDecoder.decode(xo);
                }
                else {
                    LOG.warn("Unsupported XML Type: "+xo.getClass());
                    return;
                }

                new Thread(() -> {
                    LOG.debug("Posting new object "+o.getClass());
                    eventBus.post(new SubscriptionEvent(ev.getSubscriptionId(), o));
                }).start();
            } catch (XmlException | IOException ex) {
                LOG.warn(ex.getMessage(), ex);
            }
        }
        else {
            LOG.info("Unsupported message: "+ ev.getContentType());
        }
    }
}
