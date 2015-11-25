package com.pxs.dependencies.controllers;

import static org.springframework.http.ResponseEntity.ok;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

	@RequestMapping("/")
	public HttpEntity<ResourceSupport> getIndex() {
		ResourceSupport resource = new ResourceSupport();
		return ok()
				.body(resource);
	}

}
