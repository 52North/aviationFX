
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
        Assert.assertThat(polygon.size(), CoreMatchers.is(33));
        Assert.assertThat(polygon.get(0).getLongitude(), CoreMatchers.is(-120.498962));
        Assert.assertThat(polygon.get(0).getLatitude(), CoreMatchers.is(39.6591305852));
        Assert.assertThat(polygon.get(31).getLongitude(), CoreMatchers.is(-120.5672497301));
        Assert.assertThat(polygon.get(31).getLatitude(), CoreMatchers.is(39.6539324027));
        Assert.assertThat(polygon.get(32).getLongitude(), CoreMatchers.is(-120.498962));
        Assert.assertThat(polygon.get(32).getLatitude(), CoreMatchers.is(39.6591305852));
        Assert.assertThat(a.getIdentifier(), CoreMatchers.equalTo("028e6905-f99a-4ca7-a736-2c0787cdcf57"));
        Assert.assertThat(a.getType(), CoreMatchers.equalTo("P"));
        Assert.assertThat(a.getAnnotationNote(), CoreMatchers.equalTo("SAR activities during fire fighting"));
    }

}
