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

package be.ordina.msdashboard.security.filter;

import be.ordina.msdashboard.security.strategies.BasicAuthentication;
import be.ordina.msdashboard.security.strategies.JWTAuthentication;
import be.ordina.msdashboard.security.strategy.SecurityProtocol;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Authentication filter to generate the proper authentication object.
 * Based on the header information of the incoming request and security protocol of the aggregator
 *
 * @author Kevin van Houtte
 */
public abstract class AuthFilter extends GenericFilterBean {

	private String securityProtocol = SecurityProtocol.NONE.name();

	private static final String PASSTHRU = "passthru";

	@Override
	public void doFilter(final ServletRequest req,
						 final ServletResponse res,
						 final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) req;
		String authHeader = request.getHeader("Authorization");
		if (authHeader != null) {
			if (securityProtocol.equalsIgnoreCase(SecurityProtocol.JWT.name())) {
				Authentication authentication = new UsernamePasswordAuthenticationToken(PASSTHRU, PASSTHRU);
				String token = HeaderTokenExtractor.extractBearer(authHeader);
				JWTAuthentication jwtAuthentication = new JWTAuthentication(request, authentication);
				request.setAttribute(AuthenticationDetails.ACCESS_TOKEN_TYPE, "Bearer");
				request.setAttribute(AuthenticationDetails.ACCESS_TOKEN_VALUE, token);
				jwtAuthentication.setDetails(new AuthenticationDetails(request));
				SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
			} else if (securityProtocol.equalsIgnoreCase(SecurityProtocol.BASIC.name())) {
				Authentication authentication = new UsernamePasswordAuthenticationToken(PASSTHRU, PASSTHRU);
				String token = HeaderTokenExtractor.extractBasic(authHeader);
				BasicAuthentication basicAuthentication = new BasicAuthentication(request, authentication);
				request.setAttribute(AuthenticationDetails.ACCESS_TOKEN_TYPE, "Basic");
				request.setAttribute(AuthenticationDetails.ACCESS_TOKEN_VALUE, token);
				basicAuthentication.setDetails(new AuthenticationDetails(request));
				SecurityContextHolder.getContext().setAuthentication(basicAuthentication);
			}
		}
		chain.doFilter(req, res);
	}

	protected void setSecurityProtocol(String protocol) {
		this.securityProtocol = protocol;
	}

}
