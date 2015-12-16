package com.pxs.dependencies.controllers;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pxs.dependencies.services.DependenciesResourceService;
import com.pxs.dependencies.services.VirtualDependenciesService;

@RestController
public class DependenciesController {

	@Autowired
	private DependenciesResourceService dependenciesResourceService;

	@Autowired
	private VirtualDependenciesService virtualDependenciesService;

	@RequestMapping(value = "/graph", produces = "application/json")
	public HttpEntity<Map<String, Object>> getDependenciesGraphJson() {
		return ok().body(dependenciesResourceService.getDependenciesGraphResourceJson());
	}

	@RequestMapping(value = "/table", produces = "application/json")
	public HttpEntity<Map<String, Map<String, Object>>> getDependenciesJson() {
		return ok().body(dependenciesResourceService.getDependenciesResourceJson());
	}

	@RequestMapping(value = "/node/{nodename}", method = POST)
	public void createDependency(@PathVariable("nodename") final String nodename, @RequestBody final List<String> dependencies) {
		virtualDependenciesService.createNewNode(nodename, dependencies);
	}

	@RequestMapping(value = "/node/{nodename}", method = PUT)
	public void updateDependency(@PathVariable("nodename") final String nodename, @RequestBody final List<String> dependencies) {
		virtualDependenciesService.updateNode(nodename, dependencies);
	}
}
