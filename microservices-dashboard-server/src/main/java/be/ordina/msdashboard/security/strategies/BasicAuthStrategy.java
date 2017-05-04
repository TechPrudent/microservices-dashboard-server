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

import be.ordina.msdashboard.security.filter.AuthenticationDetails;
import be.ordina.msdashboard.security.strategy.SecurityProtocol;
import be.ordina.msdashboard.security.strategy.SecurityStrategy;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Applies Basic auth headers to the outgoing aggregator requests if basic security is configured
 *
 * @author Kevin van Houtte
 */
@SecurityStrategy(type = SecurityProtocolStrategy.class, protocol = SecurityProtocol.BASIC)
public class BasicAuthStrategy implements SecurityProtocolStrategy {

	@Override
	public void apply(HttpClientRequest<ByteBuf> request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof BasicAuthentication) {
			Object details = authentication.getDetails();
			if (details instanceof AuthenticationDetails) {
				AuthenticationDetails auth = (AuthenticationDetails) details;
				String accessToken = auth.getTokenValue();
				String tokenType = auth.getTokenType() == null ? "Basic" : auth.getTokenType();
				request.withHeader("Authorization", tokenType + " " + accessToken);
			}
		}
	}
}
