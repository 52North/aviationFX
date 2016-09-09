
package org.n52.aviation.aviationfx.coding;

import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.n52.aviation.aviationfx.model.Polygon;
import org.n52.aviation.aviationfx.model.Position;
import org.n52.aviation.aviationfx.subscribe.SubscribeOptions;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscribeEncoderTest {


    @Test
    public void testEncoding() {
        SubscribeOptions options = new SubscribeOptions("my-host.url", "pubpubpub", "amq-bee", new Polygon(Arrays.asList(
                new Position[]{new Position(52.1, 7.1), new Position(52.2, 7.2), new Position(52.3, 7.3), new Position(52.1, 7.1)}
        )));

        EnvelopeDocument subDoc = new SubscribeEncoder().encodeSubscription(options, "localhost");
        Assert.assertThat(subDoc, CoreMatchers.notNullValue());
    }

}
