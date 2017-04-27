package be.ordina.msdashboard.security.filter;

import be.ordina.msdashboard.security.strategies.BasicAuthentication;
import be.ordina.msdashboard.security.strategies.JWTAuthentication;
import be.ordina.msdashboard.security.strategy.SecurityProtocol;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import static org.assertj.core.api.Assertions.assertThat;


public class AuthFilterTest {

    private AuthFilter filter;

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private MockFilterChain filterChain;

    @Before
    public void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @Test
    public void filterGeneratesAJWTAuthentication() throws Exception {
        initialiseJWTAuthFilter();
        addBearerSecurity();
        filter.doFilter(request, response, filterChain);
        assertJWTAuthentication();
    }

    @Test
    public void filterGeneratesABasicAuthentication() throws Exception {
        initialiseBasicAuthFilter();
        addBasicSecurity();
        filter.doFilter(request, response, filterChain);
        assertBasicAuthentication();
    }

    @Test
    public void authFilterWithoutBearerHeaderAndWithJWTFilterShouldNotFilter() throws Exception {
        initialiseJWTAuthFilter();
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    @Test(expected = AuthenticationServiceException.class)
    public void authFilterWithBasicHeaderAndWithJWTFilterShouldThrowAuthenticationServiceException() throws Exception {
        initialiseJWTAuthFilter();
        addBasicSecurity();
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    @Test(expected = AuthenticationServiceException.class)
    public void authFilterWithJWTHeaderAndWithBasicFilterShouldThrowAuthenticationServiceException() throws Exception {
        initialiseBasicAuthFilter();
        addBearerSecurity();
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    @Test(expected = AuthenticationServiceException.class)
    public void authFilterWithWrongHeaderAndWithBasicFilterShouldThrowAuthenticationServiceException() throws Exception {
        initialiseBasicAuthFilter();
        request.addHeader("Authorization", "wrongheader");
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    @Test(expected = AuthenticationServiceException.class)
    public void authFilterWithWrongHeaderAndWithJWTFilterShouldThrowAuthenticationServiceException() throws Exception {
        initialiseJWTAuthFilter();
        request.addHeader("Authorization", "wrongheader");
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    @Test(expected = AuthenticationServiceException.class)
    public void authFilterWithWrongAuthTypeAndWithBasicFilterShouldThrowAuthenticationServiceException() throws Exception {
        initialiseBasicAuthFilter();
        request.addHeader("Authorization", "Basi eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    @Test(expected = AuthenticationServiceException.class)
    public void authFilterWithWrongAuthTypeAndWithJWTFilterShouldThrowAuthenticationServiceException() throws Exception {
        initialiseBasicAuthFilter();
        request.addHeader("Authorization", "Beare eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    @Test
    public void authFilterWithOAuth2TypeShouldNotFilter() throws Exception {
        filter = new AuthHealthFilter(SecurityProtocol.OAUTH2.name());
        addBearerSecurity();
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    @Test
    public void authFilterWithNoneTypeShouldNotFilter() throws Exception {
        filter = new AuthHealthFilter(SecurityProtocol.NONE.name());
        addBearerSecurity();
        filter.doFilter(request, response, filterChain);
        assertNoAuthentication();
    }

    private void initialiseBasicAuthFilter() {
        filter = new AuthHealthFilter(SecurityProtocol.BASIC.name());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private void initialiseJWTAuthFilter() {
        filter = new AuthHealthFilter(SecurityProtocol.JWT.name());
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    private void addBearerSecurity() {
        request.addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
    }

    private void addBasicSecurity() {
        request.addHeader("Authorization", "Basic eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWV9.TJVA95OrM7E2cBab30RMHrHDcEfxjoYZgeFONFh7HgQ");
    }

    private void assertBasicAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isInstanceOf(BasicAuthentication.class);
        assertThat(context.getAuthentication()).isNotInstanceOf(JWTAuthentication.class);
        assertThat(context.getAuthentication()).isNotInstanceOf(OAuth2Authentication.class);
    }

    private void assertJWTAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isInstanceOf(JWTAuthentication.class);
        assertThat(context.getAuthentication()).isNotInstanceOf(BasicAuthentication.class);
        assertThat(context.getAuthentication()).isNotInstanceOf(OAuth2Authentication.class);
    }

    private void assertNoAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNull();
    }
}
