package org.n52.aviation.aviationfx.subscribe;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
class SubscribeFailedEvent {

    private final SubscribeFailedException exception;

    public SubscribeFailedEvent(SubscribeFailedException ex) {
        this.exception = ex;
    }

    public SubscribeFailedException getException() {
        return exception;
    }

}
