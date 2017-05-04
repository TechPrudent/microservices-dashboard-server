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

import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;

/**
 * Authentication details for storing tokens and sessions
 *
 * @author Kevin van Houtte
 */
@Component
public class AuthenticationDetails implements Serializable {

	private static final long serialVersionUID = -4809832298438307409L;
	public static final String ACCESS_TOKEN_VALUE = AuthenticationDetails.class.getSimpleName() + ".ACCESS_TOKEN_VALUE";
	public static final String ACCESS_TOKEN_TYPE = AuthenticationDetails.class.getSimpleName() + ".ACCESS_TOKEN_TYPE";
	private final String remoteAddress;
	private final String sessionId;
	private final String tokenValue;
	private final String tokenType;
	private final String display;
	private Object decodedDetails;

	public AuthenticationDetails(HttpServletRequest request) {
		this.tokenValue = (String) request.getAttribute(ACCESS_TOKEN_VALUE);
		this.tokenType = (String) request.getAttribute(ACCESS_TOKEN_TYPE);
		this.remoteAddress = request.getRemoteAddr();
		HttpSession session = request.getSession(false);
		this.sessionId = session != null ? session.getId() : null;
		StringBuilder builder = new StringBuilder();
		if (this.remoteAddress != null) {
			builder.append("remoteAddress=").append(this.remoteAddress);
		}
		if (builder.length() > 1) {
			builder.append(", ");
		}
		if (this.sessionId != null) {
			builder.append("sessionId=<SESSION>");
			if (builder.length() > 1) {
				builder.append(", ");
			}
		}
		if (this.tokenType != null) {
			builder.append("tokenType=").append(this.tokenType);
		}

		if (this.tokenValue != null) {
			builder.append("tokenValue=<TOKEN>");
		}
		this.display = builder.toString();
	}

	public String getTokenValue() {
		return this.tokenValue;
	}

	public String getTokenType() {
		return this.tokenType;
	}

	public String getRemoteAddress() {
		return this.remoteAddress;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public Object getDecodedDetails() {
		return this.decodedDetails;
	}

	public void setDecodedDetails(Object decodedDetails) {
		this.decodedDetails = decodedDetails;
	}

	public String toString() {
		return this.display;
	}

	public int hashCode() {
		int resultOne = 1;
		int result = 31 * resultOne + (this.sessionId == null ? 0 : this.sessionId.hashCode());
		result = 31 * result + (this.tokenType == null ? 0 : this.tokenType.hashCode());
		result = 31 * result + (this.tokenValue == null ? 0 : this.tokenValue.hashCode());
		return result;
	}


	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		} else {
			AuthenticationDetails other = (AuthenticationDetails) obj;
			if (this.sessionId == null) {
				if (other.sessionId != null) {
					return false;
				}
			} else if (!this.sessionId.equals(other.sessionId)) {
				return false;
			}
			if (this.tokenType == null) {
				if (other.tokenType != null) {
					return false;
				}
			} else if (!this.tokenType.equals(other.tokenType)) {
				return false;
			}
			if (this.tokenValue == null) {
				if (other.tokenValue != null) {
					return false;
				}
			} else if (!this.tokenValue.equals(other.tokenValue)) {
				return false;
			}

			return true;
		}
	}
}


