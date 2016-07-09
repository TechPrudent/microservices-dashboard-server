package be.ordina.msdashboard.aggregators.pact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andreas Evers
 */
public class PactProperties {

    private Map<String, String> requestHeaders = new HashMap<>();
    private List<String> filteredServices = new ArrayList<>();

    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    //TODO: Apply this
    public List<String> getFilteredServices() {
        return filteredServices;
    }
}
