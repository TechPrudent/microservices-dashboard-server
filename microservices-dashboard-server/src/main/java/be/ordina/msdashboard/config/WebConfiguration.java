package be.ordina.msdashboard.config;

import be.ordina.msdashboard.cache.CacheCleaningBean;
import be.ordina.msdashboard.controllers.NodesController;
import be.ordina.msdashboard.properties.Labels;
import be.ordina.msdashboard.services.DependenciesResourceService;
import be.ordina.msdashboard.store.NodeStore;
import be.ordina.msdashboard.store.RedisStore;
import be.ordina.msdashboard.store.SimpleStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public NodeStore nodeStore;

    @Autowired
    public CacheCleaningBean cacheCleaningBean;

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
        return new DependenciesResourceService();
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
