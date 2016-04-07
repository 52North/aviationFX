package org.n52.aviation.aviationfx.consume;

/**
 *
 * @author Matthes Rieke m.rieke@52north.org
 */
public class NewMessageEvent {

    private final Object message;

    public NewMessageEvent(Object value) {
        this.message = value;
    }

    public Object getMessage() {
        return message;
    }

}
