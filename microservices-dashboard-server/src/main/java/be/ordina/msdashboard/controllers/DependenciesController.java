package be.ordina.msdashboard.controllers;

import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Map;

import be.ordina.msdashboard.services.DependenciesResourceService;
import be.ordina.msdashboard.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;

import be.ordina.msdashboard.model.Node;

@CrossOrigin(maxAge = 3600)
@RestController
public class DependenciesController {

	@Autowired
	private DependenciesResourceService dependenciesResourceService;

	@Autowired
	private RedisService redisService;

	@RequestMapping(value = "/graph", produces = "application/json")
	public HttpEntity<Map<String, Object>> getDependenciesGraphJson() {
		return ok().body(dependenciesResourceService.getDependenciesGraphResourceJson());
	}

	@RequestMapping(value = "/table", produces = "application/json")
	public HttpEntity<Node> getDependenciesJson() {
		return ok().body(dependenciesResourceService.getDependenciesResourceJson());
	}

	@RequestMapping(value = "/node", method = POST)
	public void saveNode(@RequestBody final String nodeData) {
		redisService.saveNode(nodeData);
	}

	@RequestMapping(value = "/node", method = GET)
	public List<Node> getAllNodes() {
		return redisService.getAllNodes();
	}

	@RequestMapping(value = "/node/{nodeId}", method = DELETE)
	public void deleteNode(@PathVariable String nodeId) {
		redisService.deleteNode(nodeId);
	}

	@RequestMapping(value = "/node", method = DELETE)
	public void deleteAllNodes() {
		redisService.deleteAllNodes();
	}

	@RequestMapping(value = "/flush", method = DELETE)
	public void flushAll() {
		redisService.flushDB();
	}

	@RequestMapping(value = "evictCache", method = POST)
	public void evictCache (){
		redisService.evictCache();
	}
}
