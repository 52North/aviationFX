package org.n52.aviation.aviationfx.subscribe;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class SubscribeFailedException extends Exception {

    public SubscribeFailedException(String message) {
        super(message);
    }

    public SubscribeFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
