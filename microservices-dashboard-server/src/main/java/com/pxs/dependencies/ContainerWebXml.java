package com.pxs.dependencies;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

/**
 * Working without web.xml with container (not embedded mode).
 * JBOSS EAP 6.2 specific: you need to map dispatcherServlet to /* .
 */
public class ContainerWebXml extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
		return application.sources(Application.class);
	}
}
