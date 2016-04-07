package org.n52.aviation.aviationfx.consume;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class AmqpConnectionCreationFailedException extends Exception {

    public AmqpConnectionCreationFailedException(String message) {
        super(message);
    }

    public AmqpConnectionCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AmqpConnectionCreationFailedException(Throwable cause) {
        super(cause);
    }

}
