package be.ordina.msdashboard.nodes.aggregators.health;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andreas Evers
 */
public class HealthProperties {

    private Map<String, String> requestHeaders = new HashMap<>();
    private List<String> filteredServices = new ArrayList<>();

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    public List<String> getFilteredServices() {
        return filteredServices;
    }
}
