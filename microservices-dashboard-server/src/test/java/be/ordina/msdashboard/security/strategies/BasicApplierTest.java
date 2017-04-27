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

package be.ordina.msdashboard.security.strategies;

import be.ordina.msdashboard.security.filter.AuthHealthFilter;
import be.ordina.msdashboard.security.filter.AuthenticationDetails;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import static org.mockito.Matchers.anyString;


@RunWith(PowerMockRunner.class)
public class BasicApplierTest {

    private final BasicAuthApplier basicAuthApplier = new BasicAuthApplier();

    private final JWTApplier jwtApplier = new JWTApplier();

    private final OAuth2Applier oAuth2Applier = new OAuth2Applier();

    @Mock
    private AuthHealthFilter filter;

    @Mock
    private HttpClientRequest<ByteBuf> request;

    private MockHttpServletRequest mockRequest;

    private MockHttpServletResponse response;

    private MockFilterChain filterChain;

    private final static String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ";

    private final static String BASIC_TYPE = "Basic";

    private final static String BEARER_TYPE = "Bearer";

    @Before
    public void setUp() {
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        mockRequest = new MockHttpServletRequest();
        mockRequest.addHeader("Authorization", ACCESS_TOKEN);
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    public void basicApplierAppliesCorrectBasicHeader() throws Exception {
        mockAuthFilterWithBasicAuthentication();
        filter.doFilter(mockRequest, response, filterChain);
        basicAuthApplier.apply(request);
        Mockito.verify(request, Mockito.times(1)).withHeader("Authorization", BASIC_TYPE + " " + ACCESS_TOKEN);
    }

    @Test
    public void basicApplierDoesNotApplyWithoutAuthentication() throws Exception {
        basicAuthApplier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    @Test
    public void basicApplierDoesNotApplyWithJWTAuthentication() throws Exception {
        mockAuthFilterWithJWTAuthentication();
        filter.doFilter(mockRequest, response, filterChain);
        basicAuthApplier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    @Test
    public void basicApplierDoesNotApplyWithOAuth2Authentication() throws Exception {
        mockOAuth2authentication();
        filter.doFilter(mockRequest, response, filterChain);
        basicAuthApplier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    @Test
    public void jwtApplierAppliesCorrectBearerHeader() throws Exception {
        mockAuthFilterWithJWTAuthentication();
        filter.doFilter(mockRequest, response, filterChain);
        jwtApplier.apply(request);
        Mockito.verify(request, Mockito.times(1)).withHeader("Authorization", BEARER_TYPE + " " + ACCESS_TOKEN);
    }

    @Test
    public void jwtApplierDoesNotApplyWithoutAuthentication() throws Exception {
        jwtApplier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    @Test
    public void jwtApplierDoesNotApplyWithBasicAuthentication() throws Exception {
        mockAuthFilterWithBasicAuthentication();
        filter.doFilter(mockRequest, response, filterChain);
        jwtApplier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    @Test
    public void jwtApplierDoesNotApplyWithOAuth2Authentication() throws Exception {
        mockOAuth2authentication();
        filter.doFilter(mockRequest, response, filterChain);
        jwtApplier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    @Test
    public void oauth2ApplierAppliesCorrectBearerHeader() throws Exception {
        mockOAuth2authentication();
        filter.doFilter(mockRequest, response, filterChain);
        oAuth2Applier.apply(request);
        Mockito.verify(request, Mockito.times(1)).withHeader("Authorization", BEARER_TYPE + " " + ACCESS_TOKEN);
    }

    @Test
    public void oauth2ApplierDoesNotApplyWithoutAuthentication() throws Exception {
        oAuth2Applier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    @Test
    public void oauth2ApplierDoesNotApplyWithBasicAuthentication() throws Exception {
        mockAuthFilterWithBasicAuthentication();
        filter.doFilter(mockRequest, response, filterChain);
        oAuth2Applier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    @Test
    public void oauth2ApplierDoesNotApplyWithJWTAuthentication() throws Exception {
        mockAuthFilterWithJWTAuthentication();
        filter.doFilter(mockRequest, response, filterChain);
        oAuth2Applier.apply(request);
        Mockito.verify(request, Mockito.times(0)).withHeader(anyString(), anyString());
    }

    private void mockAuthFilterWithBasicAuthentication() throws Exception {
        Mockito.doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            MockHttpServletRequest mockAnswerRequest = (MockHttpServletRequest) args[0];
            Authentication authentication = new UsernamePasswordAuthenticationToken("Test", "Test");
            BasicAuthentication basicAuthentication = new BasicAuthentication(mockAnswerRequest, authentication);
            mockAnswerRequest.setAttribute(AuthenticationDetails.ACCESS_TOKEN_TYPE, BASIC_TYPE);
            mockAnswerRequest.setAttribute(AuthenticationDetails.ACCESS_TOKEN_VALUE, ACCESS_TOKEN);
            basicAuthentication.setDetails(new AuthenticationDetails(mockAnswerRequest));
            SecurityContextHolder.getContext().setAuthentication(basicAuthentication);
            return null;
        }).when(filter).doFilter(mockRequest, response, filterChain);
    }

    private void mockAuthFilterWithJWTAuthentication() throws Exception {
        Mockito.doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            MockHttpServletRequest mockAnswerRequest = (MockHttpServletRequest) args[0];
            Authentication authentication = new UsernamePasswordAuthenticationToken("Test", "Test");
            JWTAuthentication jwtAuthentication = new JWTAuthentication(mockAnswerRequest, authentication);
            mockAnswerRequest.setAttribute(AuthenticationDetails.ACCESS_TOKEN_TYPE, "Bearer");
            mockAnswerRequest.setAttribute(AuthenticationDetails.ACCESS_TOKEN_VALUE, ACCESS_TOKEN);
            jwtAuthentication.setDetails(new AuthenticationDetails(mockAnswerRequest));
            SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
            return null;
        }).when(filter).doFilter(mockRequest, response, filterChain);
    }

    private void mockOAuth2authentication() throws Exception {
        Authentication user = new UsernamePasswordAuthenticationToken("user", "password");
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setClientId("client");
        OAuth2Request oAuth2Request = authorizationRequest.createOAuth2Request();
        OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, user);
        mockRequest.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, BEARER_TYPE);
        mockRequest.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, ACCESS_TOKEN);
        oAuth2Authentication.setDetails(new OAuth2AuthenticationDetails(mockRequest));
        SecurityContext securityContextHolder = SecurityContextHolder.getContext();
        securityContextHolder.setAuthentication(oAuth2Authentication);
    }
}
