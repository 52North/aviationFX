
package org.n52.aviation.aviationfx.coding;

import com.vividsolutions.jts.geom.LineString;
import java.io.IOException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AirspaceDecoderTest {

    @Test
    public void testDecoding() throws IOException {
        AirspaceDecoder decoder = new AirspaceDecoder();
        Airspace a = decoder.decode(getClass().getResourceAsStream("/test-saa.xml"));

        Assert.assertThat(a, CoreMatchers.notNullValue());
        Assert.assertThat(a.getPolygon(), CoreMatchers.notNullValue());

        LineString polygon = a.getPolygon().getExteriorRing();
        Assert.assertThat(polygon.getCoordinates().length, CoreMatchers.is(7));
        Assert.assertThat(polygon.getCoordinates()[0].x, CoreMatchers.is(-118.67980957031249));
        Assert.assertThat(polygon.getCoordinates()[0].y, CoreMatchers.is(34.54163119530972));
        Assert.assertThat(polygon.getCoordinates()[6].x, CoreMatchers.is(-118.67980957031249));
        Assert.assertThat(polygon.getCoordinates()[6].y, CoreMatchers.is(34.54163119530972));

    }

}
