
package org.n52.aviation.aviationfx.coding;

import org.n52.aviation.aviationfx.model.Flight;
import org.n52.aviation.aviationfx.model.Route;
import java.io.IOException;
import java.io.InputStream;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class FlightDecoderTest {

    @Test
    public void testDecodingComplex() throws IOException {
        FlightDecoder decoder = new FlightDecoder();

        Flight flight = decoder.decode(readFlight("/test-flight.xml"));

        Assert.assertThat(flight.getGufi(), CoreMatchers.is("8c7995c5-1a65-430c-96d8-a8347b9ed2a3"));
        Assert.assertThat(flight.getIdentification(), CoreMatchers.is("MNG200D"));
        Assert.assertThat(flight.getBearing(), CoreMatchers.is(62.2));
        Assert.assertThat(flight.getCurrentPosition().getLatitude(), CoreMatchers.is(35.15));
        Assert.assertThat(flight.getCurrentPosition().getLongitude(), CoreMatchers.is(-119.38));

        Assert.assertThat(flight.getRoute(), CoreMatchers.notNullValue());
        Route route = flight.getRoute();
        Assert.assertThat(route.getPositionList().size(), CoreMatchers.is(3));
        Assert.assertThat(route.getPositionList().get(0).getLatitude(), CoreMatchers.is(34.31281554905527));
        Assert.assertThat(route.getPositionList().get(0).getLongitude(), CoreMatchers.is(-118.89953613281249));
    }


    @Test
    public void testDecodingSimple() throws IOException {
        FlightDecoder decoder = new FlightDecoder();

        Flight flight = decoder.decode(readFlight("/test-flight2.xml"));

        Assert.assertThat(flight.getGufi(), CoreMatchers.is("gugugufi"));
        Assert.assertThat(flight.getBearing(), CoreMatchers.is(177.4900360107422));
        Assert.assertThat(flight.getCurrentPosition().getLatitude(), CoreMatchers.is(40.870833));
        Assert.assertThat(flight.getCurrentPosition().getLongitude(), CoreMatchers.is(-120.910833));

    }

    private InputStream readFlight(String rs) {
        return getClass().getResourceAsStream(rs);
    }

}
