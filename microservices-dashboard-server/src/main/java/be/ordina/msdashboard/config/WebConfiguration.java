/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.ordina.msdashboard.config;

import be.ordina.msdashboard.aggregators.NodeAggregator;
import be.ordina.msdashboard.aggregators.VirtualAndRealDependencyIntegrator;
import be.ordina.msdashboard.aggregators.health.HealthIndicatorsAggregator;
import be.ordina.msdashboard.aggregators.health.HealthProperties;
import be.ordina.msdashboard.aggregators.index.IndexToNodeConverter;
import be.ordina.msdashboard.aggregators.index.IndexesAggregator;
import be.ordina.msdashboard.aggregators.pact.PactsAggregator;
import be.ordina.msdashboard.cache.CacheCleaningBean;
import be.ordina.msdashboard.controllers.NodesController;
import be.ordina.msdashboard.properties.Labels;
import be.ordina.msdashboard.services.DependenciesResourceService;
import be.ordina.msdashboard.stores.NodeStore;
import be.ordina.msdashboard.stores.SimpleStore;
import be.ordina.msdashboard.uriresolvers.DefaultUriResolver;
import be.ordina.msdashboard.uriresolvers.EurekaUriResolver;
import be.ordina.msdashboard.uriresolvers.UriResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase.*;

/**
 * @author Andreas Evers
 */
@Configuration
@EnableConfigurationProperties
@AutoConfigureAfter({ RedisConfiguration.class })
public class WebConfiguration extends WebMvcConfigurerAdapter {

    @Bean
    @ConditionalOnMissingBean
    public Labels labels() {
        return new Labels();
    }

    @Configuration
    @AutoConfigureAfter({ EurekaConfiguration.class, AggregatorsConfiguration.class })
    public static class DependenciesResourceConfiguration {

        @Autowired
        private NodeStore nodeStore;

        @Autowired
        private CacheCleaningBean cacheCleaningBean;

        @Autowired(required = false)
        private List<NodeAggregator> aggregators = new ArrayList<>();

        @Bean
        public DependenciesResourceService dependenciesResourceService() {
            return new DependenciesResourceService(aggregators, nodeStore);
        }

        @Bean
        public NodesController nodesController() {
            return new NodesController(dependenciesResourceService(), nodeStore, cacheCleaningBean);
        }

    }

    @Configuration
    @ConditionalOnProperty("eureka.client.serviceUrl.defaultZone")
    public static class EurekaConfiguration {

        @Autowired
        private DiscoveryClient discoveryClient;

        @Bean
        public HealthIndicatorsAggregator healthIndicatorsAggregator(Environment environment) {
            return new HealthIndicatorsAggregator(discoveryClient, uriResolver(), healthProperties());
        }

        @ConfigurationProperties("msdashboard.health")
        @Bean
        public HealthProperties healthProperties() {
            return new HealthProperties();
        }

        @Bean
        public IndexesAggregator indexesAggregator() {
            return new IndexesAggregator(new IndexToNodeConverter(), discoveryClient, uriResolver());
        }

        @Bean
        @ConditionalOnMissingBean
        public UriResolver uriResolver() {
            return new EurekaUriResolver();
        }

    }

    @Configuration
    public static class AggregatorsConfiguration {

        @Bean
        @ConditionalOnProperty("pact-broker.url")
        public PactsAggregator pactsAggregator() {
            return new PactsAggregator();
        }
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "eureka.client.serviceUrl.defaultZone", matchIfMissing = true)
    public UriResolver uriResolver() {
        return new DefaultUriResolver();
    }

}
