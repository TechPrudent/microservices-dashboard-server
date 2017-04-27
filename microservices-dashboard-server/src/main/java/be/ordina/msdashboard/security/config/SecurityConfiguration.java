/*
 * Copyright 2012-2017 the original author or authors.
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

package be.ordina.msdashboard.security.config;

import be.ordina.msdashboard.security.strategies.BasicAuthApplier;
import be.ordina.msdashboard.security.strategies.DefaultApplier;
import be.ordina.msdashboard.security.strategies.JWTApplier;
import be.ordina.msdashboard.security.strategies.OAuth2Applier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Makes sure one bean is activated per security protocol
 *
 * @author Kevin van Houtte
 */
@Configuration
public class SecurityConfiguration {

    @ConditionalOnMissingBean
    @ConditionalOnExpression("'${msdashboard.health.security}'=='oauth2' or '${msdashboard.index.security}'=='oauth2' or '${msdashboard.pact.security}'=='oauth2' or '${msdashboard.mappings.security}'=='oauth2'")
    @Bean
    public OAuth2Applier oAuth2Applier() {
        return new OAuth2Applier();
    }

    @ConditionalOnMissingBean
    @ConditionalOnExpression("'${msdashboard.health.security}'=='jwt' or '${msdashboard.index.security}'=='jwt' or '${msdashboard.pact.security}'=='jwt' or '${msdashboard.mappings.security}'=='jwt'")
    @Bean
    public JWTApplier jwtApplier() {
        return new JWTApplier();
    }

    @ConditionalOnMissingBean
    @ConditionalOnExpression("'${msdashboard.health.security}'=='basic' or '${msdashboard.index.security}'=='basic' or '${msdashboard.pact.security}'=='basic' or '${msdashboard.mappings.security}'=='basic'")
    @Bean
    public BasicAuthApplier basicAuthApplier() {
        return new BasicAuthApplier();
    }

    @Bean
    public DefaultApplier defaultApplier() {
        return new DefaultApplier();
    }

}
