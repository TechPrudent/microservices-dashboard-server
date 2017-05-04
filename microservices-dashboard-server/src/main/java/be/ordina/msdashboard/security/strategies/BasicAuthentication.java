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

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import javax.servlet.http.HttpServletRequest;

/**
 * Basic Authentication Object to differentiate from other protocols
 *
 * @author Kevin van Houtte
 */
public class BasicAuthentication extends AbstractAuthenticationToken {
	private static final long serialVersionUID = -4809832298438307309L;
	private final HttpServletRequest storedRequest;
	private final Authentication userAuthentication;

	public BasicAuthentication(HttpServletRequest storedRequest, Authentication userAuthentication) {
		super(userAuthentication.getAuthorities());
		this.storedRequest = storedRequest;
		this.userAuthentication = userAuthentication;
	}

	public Object getCredentials() {
		return "";
	}

	public Object getPrincipal() {
		return this.userAuthentication.getPrincipal();
	}

	public HttpServletRequest getStoredRequest() {
		return this.storedRequest;
	}

	public Authentication getUserAuthentication() {
		return this.userAuthentication;
	}

	public boolean isAuthenticated() {
		return this.userAuthentication.isAuthenticated();
	}

	public void eraseCredentials() {
		super.eraseCredentials();
		if (this.userAuthentication != null && CredentialsContainer.class.isAssignableFrom(this.userAuthentication.getClass())) {
			((CredentialsContainer) CredentialsContainer.class.cast(this.userAuthentication)).eraseCredentials();
		}
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof OAuth2Authentication)) {
			return false;
		} else if (!super.equals(o)) {
			return false;
		} else {
			BasicAuthentication that = (BasicAuthentication) o;
			if (!this.storedRequest.equals(that.storedRequest)) {
				return false;
			} else {
				label33:
				{
					if (this.userAuthentication != null) {
						if (this.userAuthentication.equals(that.userAuthentication)) {
							break label33;
						}
					} else if (that.userAuthentication == null) {
						break label33;
					}
					return false;
				}
				return true;
			}
		}
	}

	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + this.storedRequest.hashCode();
		result = 31 * result + (this.userAuthentication != null ? this.userAuthentication.hashCode() : 0);
		return result;
	}
}
