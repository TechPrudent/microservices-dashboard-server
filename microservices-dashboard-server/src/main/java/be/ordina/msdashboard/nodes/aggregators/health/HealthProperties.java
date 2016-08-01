package be.ordina.msdashboard.nodes.aggregators.health;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.ordina.msdashboard.nodes.aggregators.Constants.CONFIG_SERVER;
import static be.ordina.msdashboard.nodes.aggregators.Constants.DISCOVERY;
import static be.ordina.msdashboard.nodes.aggregators.Constants.HYSTRIX;
import static be.ordina.msdashboard.nodes.aggregators.Constants.TURBINE;
import static be.ordina.msdashboard.nodes.aggregators.Constants.ZUUL;

/**
 * Properties for health aggregation.
 *
 * @author Andreas Evers
 */
public class HealthProperties {

    public static final String DISK_SPACE = "diskSpace";

    private Map<String, String> requestHeaders = new HashMap<>();

    private List<String> filteredServices = Arrays.asList(HYSTRIX, TURBINE,
            DISK_SPACE, CONFIG_SERVER, DISCOVERY, ZUUL);

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public List<String> getFilteredServices() {
        return filteredServices;
    }
}
