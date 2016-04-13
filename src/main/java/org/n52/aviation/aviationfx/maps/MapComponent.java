package org.n52.aviation.aviationfx.maps;

import org.n52.aviation.aviationfx.consume.NewMessageEvent;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public interface MapComponent {

    void initialize();

    void onNewMessage(NewMessageEvent event);

}
