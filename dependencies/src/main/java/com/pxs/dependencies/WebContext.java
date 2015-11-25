package com.pxs.dependencies;

import static com.pxs.dependencies.NameSpaceConstants.HELLO_CURI_NAMESPACE;

import org.springframework.beans.factory.annotation.Autowired;
import com.pxs.utilities.filters.ShallowEtagHeaderExclHystrixFilterRegistrationBean;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.hal.CurieProvider;
import org.springframework.hateoas.hal.DefaultCurieProvider;
import org.springframework.web.servlet.LocaleResolver;

import com.pxs.utilities.resolvers.locale.ApplicationLanguageAndAcceptHeaderLocaleResolver;

@Configuration
public class WebContext {

	@Value("${server.contextPath}")
	private String contextPath;

	@Autowired
	private ShallowEtagHeaderExclHystrixFilterRegistrationBean shallowEtagHeaderExclHystrixFilterRegistrationBean;

	@Bean
	public CurieProvider curieProvider() {
		return new DefaultCurieProvider(HELLO_CURI_NAMESPACE, new UriTemplate(contextPath + "/generated-docs/api-guide.html#resources-{rel}"));
	}

	@Bean
	public LocaleResolver localeResolver() {
		return new ApplicationLanguageAndAcceptHeaderLocaleResolver();
	}

	@Bean
	public FilterRegistrationBean shallowEtagHeaderFilter() {
		return shallowEtagHeaderExclHystrixFilterRegistrationBean.register();
	}
}
