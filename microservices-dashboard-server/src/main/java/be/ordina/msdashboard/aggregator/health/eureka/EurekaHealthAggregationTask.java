package be.ordina.msdashboard.aggregator.health.eureka;

import be.ordina.msdashboard.aggregator.health.Constants;
import be.ordina.msdashboard.node.Node;
import be.ordina.msdashboard.node.NodeBuilder;
import com.netflix.appinfo.InstanceInfo;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * A health aggregation task that retrieves the health information and composes a node.
 *
 * @author Tim Ysewyn
 */
public class EurekaHealthAggregationTask {

    private static final Logger logger = LoggerFactory.getLogger(EurekaHealthAggregationTask.class);

    private final EurekaDiscoveryClient.EurekaServiceInstance eurekaServiceInstance;

    public EurekaHealthAggregationTask(EurekaDiscoveryClient.EurekaServiceInstance eurekaServiceInstance) {
        this.eurekaServiceInstance = eurekaServiceInstance;
    }

    public Node getNode() {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = new DateTime().getMillis();
        }

        InstanceInfo instanceInfo = eurekaServiceInstance.getInstanceInfo();
        String uriString = instanceInfo.getSecureHealthCheckUrl();
        if (StringUtils.isEmpty(uriString)) {
            uriString = instanceInfo.getHealthCheckUrl();
        }

        if (StringUtils.isEmpty(uriString)) {
            logger.error("Could't retrieve the healthcheck URI for instance '{}'", eurekaServiceInstance.getServiceId());
            return null;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Fetching health info from {}", uriString);
        }

        RestTemplate restTemplate = getRestTemplate();
        ResponseEntity<EurekaHealthInfo> response = restTemplate.exchange(uriString, HttpMethod.GET, null, EurekaHealthInfo.class);

        if (!response.hasBody()) {
            logger.error("Could't retrieve the healthcheck data for instance '{}'", eurekaServiceInstance.getServiceId());
            return null;
        }

        EurekaHealthInfo eurekaHealthInfo = response.getBody();

        if (logger.isDebugEnabled()) {
            logger.debug("Health info: {}", eurekaHealthInfo);
        }

        Node node = NodeBuilder.node().withId(eurekaServiceInstance.getServiceId()).withDetail(Constants.STATUS, eurekaHealthInfo.getStatus()).build();

        if (logger.isDebugEnabled()) {
            logger.debug("Total time: {} ms", new DateTime().getMillis() - startTime);
        }

        return node;
    }

    private RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(final HttpStatus statusCode) {
                return !HttpStatus.SERVICE_UNAVAILABLE.equals(statusCode)
                        && (statusCode.series() == HttpStatus.Series.CLIENT_ERROR || statusCode.series() == HttpStatus.Series.SERVER_ERROR);
            }
        });
        return restTemplate;
    }

}
