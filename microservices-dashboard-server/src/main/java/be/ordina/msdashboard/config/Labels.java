/*
 * Copyright 2012-2016 the original author or authors.
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
 */
package be.ordina.msdashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Labels for the dashboard UI.
 *
 * @author Andreas Evers
 */
// TODO: fix this class and externalize properties better
@ConfigurationProperties("msdashboard.labels")
public class Labels {
	private String toolbox = "TOOLBOX";
	private String discovery = "discovery";
	private String configServer = "configServer";
	private String type = "type";
	private String group = "group";
	private String version = "version";
	private String lane = "lane";
	private String id = "id";
	private String details = "details";
	private String status = "status";
	private String diskSpace = "diskSpace";
	private String hystrix = "hystrix";
	private String directed = "directed";
	private String multigraph = "multigraph";
	private String graph = "graph";
	private String lanes = "lanes";
	private String types = "types";
	private String nodes = "nodes";
	private String links = "links";
	private String ui = "UI Components";
	private String resources = "Resources";
	private String microservices = "Microservices";
	private String backends = "Backends";
	private String description = "description";
	private String db = "DB";
	private String microservice = "MICROSERVICE";
	private String resource = "RESOURCE";
	private String uiComponent = "UI_COMPONENT";
	private String rest = "REST";
	private String soap = "SOAP";
	private String jms = "JMS";

	public String getToolbox() {
		return toolbox;
	}

	public void setToolbox(String toolbox) {
		this.toolbox = toolbox;
	}

	public String getDiscovery() {
		return discovery;
	}

	public void setDiscovery(String discovery) {
		this.discovery = discovery;
	}

	public String getConfigServer() {
		return configServer;
	}

	public void setConfigServer(String configServer) {
		this.configServer = configServer;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDiskSpace() {
		return diskSpace;
	}

	public void setDiskSpace(String diskSpace) {
		this.diskSpace = diskSpace;
	}

	public String getHystrix() {
		return hystrix;
	}

	public void setHystrix(String hystrix) {
		this.hystrix = hystrix;
	}

	public String getDirected() {
		return directed;
	}

	public void setDirected(String directed) {
		this.directed = directed;
	}

	public String getMultigraph() {
		return multigraph;
	}

	public void setMultigraph(String multigraph) {
		this.multigraph = multigraph;
	}

	public String getGraph() {
		return graph;
	}

	public void setGraph(String graph) {
		this.graph = graph;
	}

	public String getLanes() {
		return lanes;
	}

	public void setLanes(String lanes) {
		this.lanes = lanes;
	}

	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public String getNodes() {
		return nodes;
	}

	public void setNodes(String nodes) {
		this.nodes = nodes;
	}

	public String getLinks() {
		return links;
	}

	public void setLinks(String links) {
		this.links = links;
	}

	public String getUi() {
		return ui;
	}

	public void setUi(String ui) {
		this.ui = ui;
	}

	public String getResources() {
		return resources;
	}

	public void setResources(String resources) {
		this.resources = resources;
	}

	public String getMicroservices() {
		return microservices;
	}

	public void setMicroservices(String microservices) {
		this.microservices = microservices;
	}

	public String getBackends() {
		return backends;
	}

	public void setBackends(String backends) {
		this.backends = backends;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDb() {
		return db;
	}

	public void setDb(String db) {
		this.db = db;
	}

	public String getMicroservice() {
		return microservice;
	}

	public void setMicroservice(String microservice) {
		this.microservice = microservice;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getUiComponent() {
		return uiComponent;
	}

	public void setUiComponent(String uiComponent) {
		this.uiComponent = uiComponent;
	}

	public String getRest() {
		return rest;
	}

	public void setRest(String rest) {
		this.rest = rest;
	}

	public String getSoap() {
		return soap;
	}

	public void setSoap(String soap) {
		this.soap = soap;
	}

	public String getJms() {
		return jms;
	}

	public void setJms(String jms) {
		this.jms = jms;
	}
}
