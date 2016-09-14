
package org.n52.aviation.aviationfx.coding;

import org.n52.aviation.aviationfx.model.Airspace;
import aero.aixm.schema.x51.AbstractAIXMFeatureType;
import aero.aixm.schema.x51.AirspaceActivationPropertyType;
import aero.aixm.schema.x51.AirspaceActivationType;
import aero.aixm.schema.x51.AirspaceGeometryComponentPropertyType;
import aero.aixm.schema.x51.AirspaceTimeSlicePropertyType;
import aero.aixm.schema.x51.AirspaceTimeSliceType;
import aero.aixm.schema.x51.AirspaceType;
import aero.aixm.schema.x51.InterpretationDocument;
import aero.aixm.schema.x51.LinguisticNotePropertyType;
import aero.aixm.schema.x51.NotePropertyType;
import aero.aixm.schema.x51.SurfaceType;
import aero.aixm.schema.x51.message.AIXMBasicMessageDocument;
import aero.aixm.schema.x51.message.BasicMessageMemberAIXMPropertyType;
import com.vividsolutions.jts.geom.Polygon;
import java.io.IOException;
import java.io.InputStream;
import net.opengis.gml.x32.AbstractSurfacePatchType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.oxf.conversion.gml32.geometry.GeometryWithInterpolation;
import org.n52.oxf.conversion.gml32.xmlbeans.jts.PolygonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class AirspaceDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(AirspaceDecoder.class);

    public Airspace decode(XmlObject xo) throws IOException {
        if (xo instanceof AIXMBasicMessageDocument) {
            BasicMessageMemberAIXMPropertyType[] members = ((AIXMBasicMessageDocument) xo).getAIXMBasicMessage().getHasMemberArray();
            return findAndParseAirspace(members);
        }

        return null;
    }

    public Airspace decode(InputStream is) throws IOException {
        try {
            XmlObject xo = XmlObject.Factory.parse(is);

            return decode(xo);
        } catch (XmlException ex) {
            throw new IOException(ex);
        }
    }

    private Airspace findAndParseAirspace(BasicMessageMemberAIXMPropertyType[] members) {
        for (BasicMessageMemberAIXMPropertyType member : members) {
            AbstractAIXMFeatureType f = member.getAbstractAIXMFeature();
            if (f instanceof AirspaceType) {
                AirspaceType at = (AirspaceType) f;
                AirspaceTimeSliceType timeSlice = findSnapshotOrBaseline(at.getTimeSliceArray());
                if (timeSlice != null) {
                    AirspaceGeometryComponentPropertyType[] geoms = timeSlice.getGeometryComponentArray();
                    Polygon geometry = parseGeometry(geoms);
                    Airspace result = new Airspace(geometry);

                    if (timeSlice.isSetType()) {
                        result.setType(timeSlice.getType().getStringValue());
                    }

                    if (timeSlice.getActivationArray()!= null && timeSlice.getActivationArray().length > 0) {
                        String annotationNote = resolveAnnotationNode(timeSlice.getActivationArray());
                        result.setAnnotationNote(annotationNote);
                    }

                    result.setIdentifier(at.getIdentifier().getStringValue().trim());

                    return result;
                }
            }
        }

        return null;
    }

    private AirspaceTimeSliceType findSnapshotOrBaseline(AirspaceTimeSlicePropertyType[] timeSliceArray) {
        for (AirspaceTimeSlicePropertyType ts : timeSliceArray) {
            if (ts.getAirspaceTimeSlice().getInterpretation() == InterpretationDocument.Interpretation.BASELINE
                    || ts.getAirspaceTimeSlice().getInterpretation() == InterpretationDocument.Interpretation.SNAPSHOT) {
                return ts.getAirspaceTimeSlice();
            }
        }

        return null;
    }

    private Polygon parseGeometry(AirspaceGeometryComponentPropertyType[] geoms) {
        if (geoms != null) {
            for (AirspaceGeometryComponentPropertyType geom : geoms) {
                try {
                    SurfaceType surface = geom.getAirspaceGeometryComponent()
                        .getTheAirspaceVolume()
                        .getAirspaceVolume()
                        .getHorizontalProjection()
                        .getSurface();

                    return parseSurface(surface);
                }
                catch (RuntimeException e) {
                    LOG.warn(e.getMessage(), e);
                    return null;
                }

            }
        }

        return null;
    }

    private Polygon parseSurface(SurfaceType surface) {
        for (AbstractSurfacePatchType patch : surface.getPatches().getAbstractSurfacePatchArray()) {
            GeometryWithInterpolation polyPatch = PolygonFactory.createPolygonPatch(patch, null);
            return (Polygon) polyPatch.getGeometry();
        }

        return null;
    }

    private String resolveAnnotationNode(AirspaceActivationPropertyType[] activationArray) {
        for (AirspaceActivationPropertyType aap : activationArray) {
            AirspaceActivationType activation = aap.getAirspaceActivation();
            if (activation.getAnnotationArray() != null && activation.getAnnotationArray().length > 0) {
                for (NotePropertyType np : activation.getAnnotationArray()) {
                    if (np.getNote().getTranslatedNoteArray() != null && np.getNote().getTranslatedNoteArray().length > 0) {
                        for (LinguisticNotePropertyType lnp : np.getNote().getTranslatedNoteArray()) {
                            if (lnp.getLinguisticNote().isSetNote()) {
                                return lnp.getLinguisticNote().getNote().getStringValue().trim();
                            }
                        }
                    }
                }
            }
        }

        return null;
    }


}
