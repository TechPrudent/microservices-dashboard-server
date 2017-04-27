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

package be.ordina.msdashboard.security.strategies;

import be.ordina.msdashboard.security.strategy.SecurityProtocol;
import be.ordina.msdashboard.security.strategy.SecurityStrategy;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

/**
 * Applies OAuth2 Bearer auth headers to the outgoing aggregator requests if oauth2 security is configured
 *
 * @author Kevin van Houtte
 */
@SecurityStrategy(type = SecurityProtocolApplier.class, protocol = SecurityProtocol.OAUTH2)
public class OAuth2Applier implements SecurityProtocolApplier {

    @Override
    public void apply(HttpClientRequest<ByteBuf> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof OAuth2Authentication) {
            Object details = authentication.getDetails();
            if (details instanceof OAuth2AuthenticationDetails) {
                OAuth2AuthenticationDetails oauth = (OAuth2AuthenticationDetails) details;
                String accessToken = oauth.getTokenValue();
                String tokenType = oauth.getTokenType() == null ? "Bearer" : oauth.getTokenType();
                request.withHeader("Authorization", tokenType + " " + accessToken);
            }
        }
    }
}
