package org.n52.aviation.aviationfx.consume;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class AmqpCreationFailedException extends Exception {

    public AmqpCreationFailedException(String message) {
        super(message);
    }

    public AmqpCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmqpCreationFailedException(Throwable cause) {
        super(cause);
    }

}
