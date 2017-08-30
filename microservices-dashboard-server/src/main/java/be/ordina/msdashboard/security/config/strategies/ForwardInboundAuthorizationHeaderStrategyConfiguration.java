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
package be.ordina.msdashboard.security.config.strategies;

import be.ordina.msdashboard.security.config.StrategyBeanProvider;
import be.ordina.msdashboard.security.inbound.InboundAuthorizationHeaderCaptorFilter;
import be.ordina.msdashboard.security.outbound.AuthorizationHeaderStrategy;
import be.ordina.msdashboard.security.outbound.OutboundSecurityObjectProvider;
import be.ordina.msdashboard.security.outbound.OutboundSecurityStrategy;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Security strategy which forwards incoming authorization headers on the inbound request (the one which the user calls
 * the api with). The authorization header is copied to the outgoing aggregator requests.
 * <br>
 * The Conditional expression is used to support both yaml-style properties and property-style properties.
 * Cfr. https://github.com/spring-projects/spring-boot/issues/7483 (currently unsolved)
 * @author Andreas Evers
 */
@Conditional(ForwardInboundAuthorizationHeaderStrategyConfiguration.Condition.class)
@Configuration
public class ForwardInboundAuthorizationHeaderStrategyConfiguration {

    public static class Condition extends SpringBootCondition {
        @Override
        public ConditionOutcome getMatchOutcome(final ConditionContext context,
                                                final AnnotatedTypeMetadata metadata) {
            final RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(context.getEnvironment());
            final String propertiesProperty = resolver.getProperty("msdashboard.security.strategies.forward-inbound-auth-header", String.class);
            final String yamlProperty = resolver.getProperty("msdashboard.security.strategies.forward-inbound-auth-header[0]", String.class);
            return new ConditionOutcome(propertiesProperty != null || yamlProperty != null, "Conditional on forward-inbound-auth-header value");
        }
    }

    @ConditionalOnMissingBean
    @Bean
    public InboundAuthorizationHeaderCaptorFilter inboundAuthorizationHeaderCaptorFilter() {
        return new InboundAuthorizationHeaderCaptorFilter();
    }

    @ConditionalOnMissingBean
    @Bean
    public OutboundSecurityObjectProvider outboundSecurityObjectProvider() {
        return new OutboundSecurityObjectProvider();
    }

    @ConditionalOnMissingBean
    @Bean
    public AuthorizationHeaderStrategy authorizationHeaderStrategy() {
        return new AuthorizationHeaderStrategy();
    }

    @ConditionalOnMissingBean
    @Bean
    public StrategyBeanProvider forwardInboundAuthorizationHeaderStrategyBeanProvider(
            OutboundSecurityObjectProvider outboundSecurityObjectProvider,
            AuthorizationHeaderStrategy authorizationHeaderStrategy) {
        return new ForwardInboundAuthorizationHeaderStrategyBeanProvider(outboundSecurityObjectProvider, authorizationHeaderStrategy);
    }

    public class ForwardInboundAuthorizationHeaderStrategyBeanProvider implements StrategyBeanProvider {

        private OutboundSecurityObjectProvider outboundSecurityObjectProvider;
        private OutboundSecurityStrategy authorizationHeaderStrategy;

        @Override
        public String getType() {
            return "forward-inbound-auth-header";
        }

        public ForwardInboundAuthorizationHeaderStrategyBeanProvider(OutboundSecurityObjectProvider outboundSecurityObjectProvider,
                                    AuthorizationHeaderStrategy authorizationHeaderStrategy) {
            this.outboundSecurityObjectProvider = outboundSecurityObjectProvider;
            this.authorizationHeaderStrategy = authorizationHeaderStrategy;
        }

        @Override
        public OutboundSecurityObjectProvider getOutboundSecurityObjectProvider() {
            return outboundSecurityObjectProvider;
        }

        @Override
        public OutboundSecurityStrategy getOutboundSecurityStrategy() {
            return authorizationHeaderStrategy;
        }
    }
}
