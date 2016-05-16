
package be.ordina.msdashboard.model;

import static be.ordina.msdashboard.constants.Constants.DETAILS;
import static be.ordina.msdashboard.constants.Constants.ID;
import static be.ordina.msdashboard.constants.Constants.LANE;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Node {
	@JsonProperty(ID)
	private String id;

	@JsonProperty(DETAILS)
	private Map<String, Object> details;

	@JsonProperty(LANE)
	private Integer lane;

	@Deprecated
	@JsonProperty("linkedToNodes")
	private Set<Node> linkedToNodes;

	private Set<String> linkedToNodeIds;

	private Set<String> linkedFromNodeIds;

	public void setLane(Integer lane) {
		this.lane = lane;
	}

	public Integer getLane() {
		return lane;
	}

	public void setId(java.lang.String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public Set<Node> getLinkedToNodes() {
		if(linkedToNodes ==null){
			linkedToNodes = new HashSet<>();
		}
		return linkedToNodes;
	}

	public void setLinkedToNodes(Set<Node> linkedToNodes) {
		this.linkedToNodes = linkedToNodes;
	}

	public Set<String> getLinkedToNodeIds() {
		if(linkedToNodeIds ==null){
			linkedToNodeIds = new HashSet<>();
		}
		return linkedToNodeIds;
	}

	public void setLinkedToNodeIds(Set<String> linkedToNodeIds) {
		this.linkedToNodeIds = linkedToNodeIds;
	}

	public Set<String> getLinkedFromNodeIds() {
		if(linkedFromNodeIds ==null){
			linkedFromNodeIds = new HashSet<>();
		}
		return linkedFromNodeIds;
	}

	public void setLinkedFromNodeIds(Set<String> linkedFromNodeIds) {
		this.linkedFromNodeIds = linkedFromNodeIds;
	}

	public Map<String, Object> getDetails() {
		if(details==null){
			details = new HashMap<>();
		}
		return details;
	}

	public void setDetails(Map<String, Object> details) {
		this.details = details;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Node node = (Node) o;

		return id.equals(node.id);

	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return "Node{" +
				"details=" + details +
				", id='" + id + '\'' +
				", lane=" + lane +
				", linkedToNodes=" + linkedToNodes +
				'}';
	}
}