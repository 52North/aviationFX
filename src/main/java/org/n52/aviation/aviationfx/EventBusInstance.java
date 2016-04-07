package org.n52.aviation.aviationfx;

import com.google.common.eventbus.EventBus;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class EventBusInstance {

    private static EventBus bus;

    public static synchronized EventBus getEventBus() {
        if (bus == null) {
            bus = new EventBus();
        }

        return bus;
    }

}
