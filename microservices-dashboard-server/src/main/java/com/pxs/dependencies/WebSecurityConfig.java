package com.pxs.dependencies;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.stereotype.Component;

import com.pxs.security.GrantedAuthoritiesWebAuthenticationDetailsSource;
import com.pxs.security.PreAuthenticatedProcessingFilter;
import com.pxs.security.UserDetailsService;

@Component
@EnableWebMvcSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	GrantedAuthoritiesWebAuthenticationDetailsSource grantedAuthoritiesWebAuthenticationDetailsSource;

	private PreAuthenticatedAuthenticationProvider preAuthenticatedProvider;

	public WebSecurityConfig() {
		this.preAuthenticatedProvider = new PreAuthenticatedAuthenticationProvider();
		this.preAuthenticatedProvider.setPreAuthenticatedUserDetailsService(new UserDetailsService());
	}

	@Autowired
	public void configureGlobal(final AuthenticationManagerBuilder auth)
			throws Exception { // NOSONAR
		auth.authenticationProvider(this.preAuthenticatedProvider);
	}

	@Override
	protected void configure(final HttpSecurity http) throws Exception { // NOSONAR
		PreAuthenticatedProcessingFilter filter = new PreAuthenticatedProcessingFilter();
		filter.setAuthenticationManager(this.authenticationManager());
		filter.setAuthenticationDetailsSource(this.grantedAuthoritiesWebAuthenticationDetailsSource);
		filter.setCheckForPrincipalChanges(true);
		http.authenticationProvider(this.preAuthenticatedProvider).addFilter(filter);
		http.csrf().disable();
		http.headers().disable();
		http.sessionManagement().sessionCreationPolicy(STATELESS);
	}
}
