package com.pxs.dependencies.controllers;

import static org.springframework.http.ResponseEntity.ok;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pxs.dependencies.services.DependenciesResourceService;

@RestController
// @RequestMapping("/dependencies")
public class DependenciesController {

	@Autowired
	private DependenciesResourceService dependenciesResourceService;

	@RequestMapping(value = "/dependencies/graph", produces = "application/json")
	public HttpEntity<Map<String, Object>> getDependenciesGraphJson() {
		return ok().body(dependenciesResourceService.getDependenciesGraphResourceJson());
	}

	@RequestMapping(value = "/dependencies/table", produces = "application/json")
	public HttpEntity<Map<String, Map<String, Object>>> getDependenciesJson() {
		return ok().body(dependenciesResourceService.getDependenciesResourceJson());
	}
}
