
package org.n52.aviation.aviationfx.model;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Flight {

    private final String gufi;
    private final String identification;
    private final Position currentPosition;
    private final Route route;

    public Flight(String gufi, String identification, Position currentPosition, Route route) {
        this.gufi = gufi;
        this.identification = identification;
        this.currentPosition = currentPosition;
        this.route = route;
    }

    public Route getRoute() {
        return route;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public String getGufi() {
        return gufi;
    }

    public String getIdentification() {
        return identification;
    }


}
