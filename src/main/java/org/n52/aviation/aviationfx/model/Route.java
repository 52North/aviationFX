
package org.n52.aviation.aviationfx.model;

import java.util.List;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class Route {

    private final List<Position> positionList;

    public Route(List<Position> positionList) {
        this.positionList = positionList;
    }

    public List<Position> getPositionList() {
        return positionList;
    }


}
