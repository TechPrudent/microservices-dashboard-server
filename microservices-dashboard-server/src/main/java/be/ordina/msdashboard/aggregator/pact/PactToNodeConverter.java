package be.ordina.msdashboard.aggregator.pact;

import be.ordina.msdashboard.constants.Constants;
import be.ordina.msdashboard.model.Node;
import be.ordina.msdashboard.model.NodeBuilder;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PactToNodeConverter {

	private static final Logger LOG = LoggerFactory.getLogger(PactToNodeConverter.class);
	private static final String UP = "UP";
	private static final String relPath = "rel://";

	public Node convert(final String source, final String pactUrl) {
		List<String> paths = JsonPath.read(source, "$.interactions[*].request.path");
		String provider = JsonPath.read(source, "$.provider.name");
		String consumer = JsonPath.read(source, "$.consumer.name");

		LOG.info("Retrieved UI Component for consumer {} and producer {} with rels {}", consumer, provider, paths);

		NodeBuilder node = new NodeBuilder();
		node.withId(consumer);
		node.withLane(0);
		paths.stream().forEach(path -> {
				node.withLinkedToNodeId(convertPathToRel(path));
				// TODO: Remove when deprecation finishes
				Node linkNode = NodeBuilder.node().withId(convertPathToRel(path)).build();
				node.withLinkedToNode(linkNode);
		});
		Map<String, Object> details = new HashMap<>();
		details.put("url", pactUrl);
		//details.put("docs", pactUrl);
		details.put("type", Constants.UI_COMPONENT);
		details.put("status", UP);
		node.havingDetails(details);

		return node.build();
	}

	private String convertPathToRel(String path) {
		String rel = "";
		if (path.startsWith(relPath) || path.startsWith("/" + relPath)) {
			rel = path.substring(path.indexOf(relPath) + relPath.length());
		} else {
			rel = path;
		}
		LOG.info("Path '{}' resolved to rel '{}'", path, rel);
		return rel;
	}
}

