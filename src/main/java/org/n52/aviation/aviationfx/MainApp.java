package org.n52.aviation.aviationfx;

import com.google.common.eventbus.EventBus;
import org.n52.aviation.aviationfx.consume.AmqpConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MainApp {

    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);
    private AmqpConsumer amqpConsumer;
    private EventBus eventBus;

    private void initEventBusComponents() {
        this.amqpConsumer = new AmqpConsumer();

        //TODO do not use singletons....
        eventBus.register(this.amqpConsumer);
    }

}
