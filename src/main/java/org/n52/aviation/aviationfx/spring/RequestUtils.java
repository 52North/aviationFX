package org.n52.aviation.aviationfx.spring;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
public class RequestUtils {

    public static String resolveFullRequestUrl() throws IOException, URISyntaxException {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes()).getRequest();

        URL url = new URL(request.getRequestURL().toString());

        String scheme = url.getProtocol();
        String userInfo = url.getUserInfo();

        String path = request.getRequestURI();
        if (path != null && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        String query = request.getQueryString();

        URI uri = new URI(scheme, userInfo, url.getHost(), url.getPort(), path, query, null);
        return uri.toString();
    }

}
