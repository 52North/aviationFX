
package org.n52.aviation.aviationfx.coding;

import org.n52.aviation.aviationfx.model.Airspace;
import com.vividsolutions.jts.geom.LineString;
import java.io.IOException;
import java.util.List;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.aviation.aviationfx.model.Position;

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
        Assert.assertThat(a.getOuterRing(), CoreMatchers.notNullValue());

        List<Position> polygon = a.getOuterRing();
        Assert.assertThat(polygon.size(), CoreMatchers.is(7));
        Assert.assertThat(polygon.get(0).getLongitude(), CoreMatchers.is(-118.67980957031249));
        Assert.assertThat(polygon.get(0).getLatitude(), CoreMatchers.is(34.54163119530972));
        Assert.assertThat(polygon.get(6).getLongitude(), CoreMatchers.is(-118.67980957031249));
        Assert.assertThat(polygon.get(6).getLatitude(), CoreMatchers.is(34.54163119530972));

    }

}
