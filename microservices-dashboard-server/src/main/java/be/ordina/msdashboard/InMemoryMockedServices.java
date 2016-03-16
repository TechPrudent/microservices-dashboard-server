package be.ordina.msdashboard;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@ConditionalOnProperty(name = "services.mock", matchIfMissing = false)
@Configuration
public class InMemoryMockedServices {

    @Bean
    protected DiscoveryClient discoveryClient() {
        DiscoveryClient discoveryClient = new DiscoveryClient() {

            private List<String> services = new ArrayList<>(Arrays.asList("fat-jar-test",
                    "customer-group",
                    "cdb-customer-provider",
                    "admin",
                    "user-preferences",
                    "customer-addresses",
                    "loyalty-program",
                    "product-stock",
                    "zuul",
                    "billing-payments",
                    "crypto",
                    "order-processor",
                    "customer-management",
                    "user",
                    "user-avatar",
                    "agenda",
                    "tariff-charges",
                    "billing-transactions",
                    "customer-administrators",
                    "user-billing-structure",
                    "billing-accounts",
                    "shop-management",
                    "cdb-replicator",
                    "turbine",
                    "hystrix",
                    "awards",
                    "billing-replicator",
                    "web-feedback",
                    "user-application-trusts",
                    "customer-application-access",
                    "billing-complaints",
                    "historical-usage-prepaid",
                    "offers",
                    "customer-access-numbers",
                    "carrier-wholesale-solutions",
                    "real-time-usage-prepaid",
                    "ordering-loyalty",
                    "ordering-onedrive",
                    "ereload",
                    "customer-proximus",
                    "messenger"));

            @Override
            public String description() {
                return null;
            }

            @Override
            public ServiceInstance getLocalServiceInstance() {
                return null;
            }

            @Override
            public List<ServiceInstance> getInstances(String serviceId) {
                return Arrays.asList(createServiceInstance(serviceId),
                        createServiceInstance(serviceId));
            }

            private ServiceInstance createServiceInstance(final String name) {
                return new ServiceInstance() {
                    @Override
                    public String getServiceId() {
                        return name;
                    }

                    @Override
                    public String getHost() {
                        return "localhost";
                    }

                    @Override
                    public int getPort() {
                        return 8089;
                    }

                    @Override
                    public boolean isSecure() {
                        return false;
                    }

                    @Override
                    public URI getUri() {
                        return null;
                    }

                    @Override
                    public Map<String, String> getMetadata() {
                        return null;
                    }
                };
            }

            @Override
            public List<String> getServices() {
                return services;
            }
        };
        return discoveryClient;
    }

    @Bean
    protected InMemoryWireMock inMemoryWireMock() {
        return new InMemoryWireMock();
    }
}