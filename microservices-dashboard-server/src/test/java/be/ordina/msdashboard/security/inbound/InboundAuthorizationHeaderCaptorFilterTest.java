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
 *
 */

package be.ordina.msdashboard.security.inbound;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;


public class InboundAuthorizationHeaderCaptorFilterTest {

	private InboundAuthorizationHeaderCaptorFilter filter = new InboundAuthorizationHeaderCaptorFilter();;

	private MockHttpServletRequest request;

	private MockHttpServletResponse response;

	private MockFilterChain filterChain;

	@Before
	public void setUp() {
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		filterChain = new MockFilterChain();
	}

	@After
	public void tearDown() {
		AuthorizationHeaderHolder.set(null);
	}

	@Test
	public void filterBearerAuthentication() throws Exception {
		addBearerSecurity();
		filter.doFilter(request, response, filterChain);
		assertBearerAuthentication();
	}

	@Test
	public void filterBasicAuthentication() throws Exception {
		addBasicSecurity();
		filter.doFilter(request, response, filterChain);
		assertBasicAuthentication();
	}

	@Test
	public void authFilterWithoutHeaderAndWithFilterShouldNotFilter() throws Exception {
		filter.doFilter(request, response, filterChain);
		assertNoAuthentication();
	}

	@Test
	public void authFilterWithWrongHeaderShouldPass() throws Exception {
		request.addHeader("Authorization", "wrongheader");
		filter.doFilter(request, response, filterChain);
		assertBadAuthentication();
	}

	@Test
	public void authFilterWithIncompleteBasicHeaderShouldPass() throws Exception {
		request.addHeader("Authorization", "Basi eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
		filter.doFilter(request, response, filterChain);
		assertBadAuthentication();
	}

	@Test
	public void authFilterWithIncompleteBearerHeaderShouldPass() throws Exception {
		request.addHeader("Authorization", "Beare eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
		filter.doFilter(request, response, filterChain);
		assertBadAuthentication();
	}

	private void addBearerSecurity() {
		request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
	}

	private void addBasicSecurity() {
		request.addHeader("Authorization", "Basic eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
	}

	private void assertBasicAuthentication() {
		String retrievedHeader = AuthorizationHeaderHolder.get();
		assertThat(retrievedHeader).startsWith("Basic ");
	}

	private void assertBearerAuthentication() {
		String retrievedHeader = AuthorizationHeaderHolder.get();
		assertThat(retrievedHeader).startsWith("Bearer ");
	}

	private void assertNoAuthentication() {
		String retrievedHeader = AuthorizationHeaderHolder.get();
		assertThat(retrievedHeader).isNull();
	}

	private void assertBadAuthentication() {
		String retrievedHeader = AuthorizationHeaderHolder.get();
		assertThat(retrievedHeader).doesNotStartWith("Basic ").doesNotStartWith("Bearer ");
	}
}
