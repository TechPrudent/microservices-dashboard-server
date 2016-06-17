package be.ordina.msdashboard.config;

import be.ordina.msdashboard.aggregator.DependenciesGraphResourceJsonBuilder;
import be.ordina.msdashboard.aggregator.NodeAggregator;
import be.ordina.msdashboard.aggregator.VirtualAndRealDependencyIntegrator;
import be.ordina.msdashboard.aggregator.health.HealthIndicatorsAggregator;
import be.ordina.msdashboard.aggregator.index.IndexToNodeConverter;
import be.ordina.msdashboard.aggregator.index.IndexesAggregator;
import be.ordina.msdashboard.aggregator.pact.PactsAggregator;
import be.ordina.msdashboard.cache.CacheCleaningBean;
import be.ordina.msdashboard.controllers.NodesController;
import be.ordina.msdashboard.properties.Labels;
import be.ordina.msdashboard.services.DependenciesResourceService;
import be.ordina.msdashboard.store.NodeStore;
import be.ordina.msdashboard.store.SimpleStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Andreas Evers
 */
@Configuration
@EnableConfigurationProperties
@AutoConfigureAfter({ RedisConfiguration.class })
public class WebConfiguration extends WebMvcConfigurerAdapter implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private NodeStore nodeStore;

    @Autowired
    private CacheCleaningBean cacheCleaningBean;

    @Autowired(required = false)
    private List<NodeAggregator> aggregators = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean
    public Labels labels() {
        return new Labels();
    }

    @Bean
    public NodesController nodesController() {
        return new NodesController(dependenciesResourceService(), nodeStore, cacheCleaningBean);
    }

    @Bean
    public DependenciesResourceService dependenciesResourceService() {
        return new DependenciesResourceService(aggregators, nodeStore);
    }

    @Bean
    @ConditionalOnProperty("eureka.client.serviceUrl.defaultZone")
    public HealthIndicatorsAggregator healthIndicatorsAggregator(Environment environment) {
        return new HealthIndicatorsAggregator(discoveryClient);
    }

    @Bean
    @ConditionalOnProperty("eureka.client.serviceUrl.defaultZone")
    public IndexesAggregator indexesAggregator() {
        return new IndexesAggregator(new IndexToNodeConverter(), discoveryClient);
    }

    @Bean
    @ConditionalOnProperty("pact-broker.url")
    public PactsAggregator pactsAggregator() {
        return new PactsAggregator();
    }

    @Bean
    public VirtualAndRealDependencyIntegrator virtualAndRealDependencyIntegrator() {
        return new VirtualAndRealDependencyIntegrator();
    }

    @Bean
    @ConditionalOnMissingBean
    public NodeStore nodeStore() {
        return new SimpleStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheCleaningBean cacheCleaningBean() {
        return null;
    }
}
