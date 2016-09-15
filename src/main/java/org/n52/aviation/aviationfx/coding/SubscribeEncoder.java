
package org.n52.aviation.aviationfx.coding;

import java.util.Date;
import java.util.List;
import net.opengis.fes.x20.BinarySpatialOpType;
import net.opengis.fes.x20.FilterDocument;
import net.opengis.fes.x20.IntersectsDocument;
import net.opengis.fes.x20.LiteralDocument;
import net.opengis.fes.x20.LiteralType;
import net.opengis.fes.x20.SpatialOpsType;
import net.opengis.gml.x32.AbstractRingPropertyType;
import net.opengis.gml.x32.CoordinatesType;
import net.opengis.gml.x32.LinearRingDocument;
import net.opengis.gml.x32.LinearRingType;
import net.opengis.gml.x32.PolygonDocument;
import net.opengis.gml.x32.PolygonType;
import net.opengis.pubsub.x10.DeliveryMethodDocument;
import net.opengis.pubsub.x10.DeliveryMethodType;
import net.opengis.pubsub.x10.PublicationIdentifierDocument;
import org.apache.xmlbeans.XmlObject;
import org.n52.aviation.aviationfx.XmlBeansHelper;
import org.n52.aviation.aviationfx.model.Polygon;
import org.n52.aviation.aviationfx.model.Position;
import org.n52.aviation.aviationfx.subscribe.SubscribeOptions;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.GMLGeometryFactory;
import org.n52.oxf.xmlbeans.tools.XmlUtil;
import org.oasisOpen.docs.wsn.b2.FilterType;
import org.oasisOpen.docs.wsn.b2.MessageContentDocument;
import org.oasisOpen.docs.wsn.b2.QueryExpressionType;
import org.oasisOpen.docs.wsn.b2.SubscribeDocument;
import org.w3.x2003.x05.soapEnvelope.Body;
import org.w3.x2003.x05.soapEnvelope.EnvelopeDocument;
import org.w3.x2005.x08.addressing.AttributedURIType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class SubscribeEncoder {

    public EnvelopeDocument encodeSubscription(SubscribeOptions options, String amqpDefaultBroker) {
        String host = options.getHost();
        String pubId = options.getPubId();
        String deliveryMethod = options.getDeliveryMethod();
        Polygon areaOfInterest = options.getAreaOfInterest();
        SubscribeDocument subDoc = SubscribeDocument.Factory.newInstance();
        SubscribeDocument.Subscribe sub = subDoc.addNewSubscribe();

        sub.setInitialTerminationTime(new Date(System.currentTimeMillis() + (1800000*4)));

        EndpointReferenceType conRef = sub.addNewConsumerReference();
        AttributedURIType addr = conRef.addNewAddress();
        addr.setStringValue(amqpDefaultBroker != null ? amqpDefaultBroker : "localhost");

        PublicationIdentifierDocument pubIdDoc = PublicationIdentifierDocument.Factory.newInstance();
        pubIdDoc.setPublicationIdentifier(pubId);

        XmlBeansHelper.insertChild(sub, pubIdDoc);

        DeliveryMethodDocument delMethDoc = DeliveryMethodDocument.Factory.newInstance();
        DeliveryMethodType delMeth = delMethDoc.addNewDeliveryMethod();
        delMeth.setIdentifier(deliveryMethod);

        XmlBeansHelper.insertChild(sub, delMethDoc);

        if (areaOfInterest != null) {
            FilterType ft = sub.addNewFilter();
            FilterDocument filterDoc = FilterDocument.Factory.newInstance();
            net.opengis.fes.x20.FilterType filter = filterDoc.addNewFilter();
            IntersectsDocument intersectsDoc = IntersectsDocument.Factory.newInstance();
            BinarySpatialOpType intersects = intersectsDoc.addNewIntersects();

            LiteralDocument literalDoc = LiteralDocument.Factory.newInstance();
            LiteralType literal = literalDoc.addNewLiteral();
            literal.setType(PolygonDocument.type.getDocumentElementName());

            PolygonDocument polyDoc = PolygonDocument.Factory.newInstance();
            PolygonType poly = polyDoc.addNewPolygon();
            AbstractRingPropertyType ext = poly.addNewExterior();
            LinearRingDocument linearRingDoc = LinearRingDocument.Factory.newInstance();
            LinearRingType linearRing = linearRingDoc.addNewLinearRing();
            CoordinatesType coords = linearRing.addNewCoordinates();

            String coordinateSeparator = ",";
            String tokenSeparator = ";";
            coords.setCs(coordinateSeparator);
            coords.setTs(tokenSeparator);;
            coords.setStringValue(createCoordinatesString(areaOfInterest.getOuterRing(), coordinateSeparator, tokenSeparator));
            ext.setAbstractRing(linearRing);

            XmlUtil.qualifySubstitutionGroup(ext.getAbstractRing(),
                    LinearRingDocument.type.getDocumentElementName(), LinearRingDocument.type);


            literal.set(polyDoc);
            intersects.set(literalDoc);
            intersects.setValueReference("input/geometry");

            filter.setSpatialOps(intersects);

            XmlUtil.qualifySubstitutionGroup(filter.getSpatialOps(),
                    IntersectsDocument.type.getDocumentElementName(), IntersectsDocument.type);

            MessageContentDocument messageContentDoc = MessageContentDocument.Factory.newInstance();
            QueryExpressionType messageContent = messageContentDoc.addNewMessageContent();
            messageContent.set(filterDoc);
            messageContent.setDialect("http://www.opengis.net/fes/2.0");

            ft.set(messageContentDoc);
        }

        EnvelopeDocument envDoc = EnvelopeDocument.Factory.newInstance();
        Body body = envDoc.addNewEnvelope().addNewBody();
        body.set(subDoc);

        return envDoc;
    }


    private String createCoordinatesString(List<Position> outerRing, String coordinateSeparator, String tokenSeparator) {
        StringBuilder sb = new StringBuilder();
        for (Position p : outerRing) {
            sb.append(p.getLatitude());
            sb.append(coordinateSeparator);
            sb.append(p.getLongitude());
            sb.append(tokenSeparator);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }



}
