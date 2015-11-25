package com.pxs.dependencies.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.restdocs.RestDocumentation.document;
import static org.springframework.restdocs.hypermedia.LinkExtractors.halLinks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.ResponseEntity;
import org.springframework.restdocs.config.RestDocumentationConfigurer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.pxs.dependencies.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest("server.port=0")
@DirtiesContext
public class IndexControllerIT {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mockMvc;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(context)
				.apply(new RestDocumentationConfigurer()).build();
	}

	@Test
	public void envEndpointNotHidden() throws URISyntaxException {
		Traverson traverson = new Traverson(new URI("http://localhost:" + getPort()), MediaTypes.HAL_JSON);
		ResponseEntity<ResourceSupport> index = traverson.follow().toEntity(ResourceSupport.class);
		assertThat(index).isNotNull();
	}

	@Test
	public void example()
			throws Exception {
		mockMvc.perform(get("/"))
		.andExpect(status().isOk())
		.andExpect(content().contentType("application/hal+json"))
		.andDo(document("index-example").withLinks(halLinks()))
		.andReturn();
	}

	private int getPort() {
		return ((TomcatEmbeddedServletContainer) ((AnnotationConfigEmbeddedWebApplicationContext) context).getEmbeddedServletContainer()).getPort();
	}
}
