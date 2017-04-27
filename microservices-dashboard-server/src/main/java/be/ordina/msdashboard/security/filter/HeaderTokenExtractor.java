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

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;

/**
 * For extracting and validating the access token
 *
 * @author Kevin van Houtte
 */
class HeaderTokenExtractor {

    private final static String HEADER_BEARER_PREFIX = "Bearer ";
    private final static String HEADER_BASIC_PREFIX = "Basic ";


    public static String extractBearer(String header) {
        if (StringUtils.isBlank(header)) {
            throw new AuthenticationServiceException("Authorization header cannot be blank!");
        }
        if (header.length() < HEADER_BEARER_PREFIX.length() || !header.startsWith(HEADER_BEARER_PREFIX)) {
            throw new AuthenticationServiceException("Invalid authorization header size.");
        }
        return header.substring(HEADER_BEARER_PREFIX.length(), header.length());
    }

    public static String extractBasic(String header) {
        if (StringUtils.isBlank(header)) {
            throw new AuthenticationServiceException("Authorization header cannot be blank!");
        }
        if (header.length() < HEADER_BASIC_PREFIX.length() || !header.startsWith(HEADER_BASIC_PREFIX)) {
            throw new AuthenticationServiceException("Invalid authorization header size.");
        }
        return header.substring(HEADER_BASIC_PREFIX.length(), header.length());
    }
}
