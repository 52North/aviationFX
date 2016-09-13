package org.n52.aviation.aviationfx.spring;

import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.n52.aviation.aviationfx.model.Credentials;
import org.n52.aviation.aviationfx.model.DeliveryMethod;
import org.n52.aviation.aviationfx.model.PubSubService;
import org.n52.aviation.aviationfx.model.Publication;
import org.n52.aviation.aviationfx.subscribe.SubscribeController;
import org.n52.aviation.aviationfx.subscribe.SubscribeFailedException;
import org.n52.aviation.aviationfx.subscribe.SubscribeOptions;
import org.n52.aviation.aviationfx.subscribe.SubscriptionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author <a href="mailto:m.rieke@52north.org">Matthes Rieke</a>
 */
@RestController
@RequestMapping(produces = {"application/json"})
public class ResourcesController implements Constructable {

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesController.class);
    private List<PubSubService> services;
    private Map<String, SubscribeController> controllers;

    @Autowired
    private EventBus eventBus;

    @Override
    public void construct() {
        this.services = new ArrayList<>();
        this.services.add(new PubSubService(
                "http://ows.dev.52north.org:8080/subverse-webapp/service", true,
                null,
                null,
                null
        ));
        this.services.add(new PubSubService(
                "http://localhost:8080/subverse-webapp/service", false,
                null,
                null,
                null
        ));

        this.controllers = new HashMap<>();
        services.stream().forEach((service) -> {
            this.controllers.put(service.getHost(), new SubscribeController(service.getHost(), eventBus));
        });
    }



    @RequestMapping(value = "/api")
    public ModelAndView getResources(@RequestParam(required = false) MultiValueMap<String, String> query) throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        return new ModelAndView().addObject(createResources(fullUrl));
    }


    @RequestMapping(value = "/api/subscribe")
    public SubscriptionProperties subscribe(@RequestBody(required = true) SubscribeOptions options) throws SubscribeFailedException {
        SubscribeController controller = this.controllers.get(options.getHost());
        if (controller == null) {
            throw new IllegalArgumentException("The provided host is not supported: "+options.getHost());
        }

        SubscriptionProperties sub = controller.subscribe(options);
        return sub;
    }

    @RequestMapping(value = "/api/capabilities", method = RequestMethod.GET)
    public Map<String, Object> capabilities() throws IOException, URISyntaxException {
        Map<String, Object> result = new HashMap<>();
        int pos = 0;
        for (PubSubService s : services) {
            s.setDetails(RequestUtils.resolveFullRequestUrl().concat("/"+pos));
            pos++;
        }
        result.put("pubSubServices", services);
        return result;
    }

    @RequestMapping(value = "/api/capabilities/{pos}", method = RequestMethod.POST)
    public PubSubService capability(@PathVariable String pos, @RequestBody(required = true) Credentials creds) throws IOException, URISyntaxException {
        try {
            int position = Integer.parseInt(pos);
            if (position + 1 > services.size()) {
                throw new IllegalArgumentException("service '"+pos+"' not defined");
            }

            PubSubService service = services.get(position);
            SubscribeController controller = this.controllers.get(service.getHost());

            if (controller == null) {
                throw new IllegalArgumentException("The provided host is not supported: "+service.getHost());
            }

            LOG.info("Requesting services capabilities... with credentials: "+creds.getUser());

            controller.setCredentials(creds);
            controller.requestServiceCapabilities();

            List<DeliveryMethod> dms = controller.getDeliveryMethods().values().stream().collect(Collectors.toList());
            service.setDeliveryMethods(dms);

            List<Publication> pubs = controller.getPublications().values().stream().collect(Collectors.toList());
            service.setPublications(pubs);

            return service;
        }
        catch (NumberFormatException e) {
            throw new IOException(e);
        }
    }


    @RequestMapping("/ui")
    public void redirectToManager(HttpServletResponse httpServletResponse) throws IOException, URISyntaxException {
        String fullUrl = RequestUtils.resolveFullRequestUrl();
        httpServletResponse.setHeader("Location", fullUrl.concat("/index.html"));
        httpServletResponse.setStatus(HttpStatus.FOUND.value());
    }

    private Map<String, String> createResources(String fullUrl) {
        LOG.info("Full URL: {}", fullUrl);
        Map<String, String> resources = new HashMap<>();

        resources.put("info", "aviationFX web application");
        return resources;
    }

}
