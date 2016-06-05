package be.ordina.msdashboard.config;

import be.ordina.msdashboard.aggregator.DependenciesGraphResourceJsonBuilder;
import be.ordina.msdashboard.aggregator.DependenciesResourceJsonBuilder;
import be.ordina.msdashboard.aggregator.ObservablesToGraphConverter;
import be.ordina.msdashboard.aggregator.VirtualAndRealDependencyIntegrator;
import be.ordina.msdashboard.aggregator.health.HealthIndicatorsAggregator;
import be.ordina.msdashboard.aggregator.index.IndexesAggregator;
import be.ordina.msdashboard.aggregator.pact.PactsAggregator;
import be.ordina.msdashboard.cache.CacheCleaningBean;
import be.ordina.msdashboard.cache.CachingProperties;
import be.ordina.msdashboard.controllers.NodesController;
import be.ordina.msdashboard.properties.Labels;
import be.ordina.msdashboard.services.DependenciesResourceService;
import be.ordina.msdashboard.store.NodeStore;
import be.ordina.msdashboard.store.RedisStore;
import be.ordina.msdashboard.store.SimpleStore;
import com.netflix.discovery.converters.Auto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

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

    @Autowired
    private HealthIndicatorsAggregator healthIndicatorsAggregator;

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
        return new DependenciesResourceService(dependenciesResourceJsonBuilder(),
                dependenciesGraphResourceJsonBuilder(),
                observablesToGraphConverter());
    }

    @Bean
    public DependenciesResourceJsonBuilder dependenciesResourceJsonBuilder() {
        return new DependenciesResourceJsonBuilder(healthIndicatorsAggregator);
    }

    @Bean
    public DependenciesGraphResourceJsonBuilder dependenciesGraphResourceJsonBuilder() {
        return new DependenciesGraphResourceJsonBuilder(healthIndicatorsAggregator,
                indexesAggregator(),
                pactsAggregator(),
                nodeStore,
                virtualAndRealDependencyIntegrator());
    }

    @Bean
    public ObservablesToGraphConverter observablesToGraphConverter() {
        return new ObservablesToGraphConverter(healthIndicatorsAggregator,
                indexesAggregator(),
                pactsAggregator(),
                nodeStore);
    }

    @Bean
    public HealthIndicatorsAggregator healthIndicatorsAggregator(Environment environment) {
        return new HealthIndicatorsAggregator(environment, discoveryClient);
    }

    @Bean
    public IndexesAggregator indexesAggregator() {
        return new IndexesAggregator(discoveryClient);
    }

    @Bean
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
