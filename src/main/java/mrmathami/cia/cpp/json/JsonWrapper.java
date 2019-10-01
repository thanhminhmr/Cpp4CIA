package mrmathami.cia.cpp.json;

import com.fit.cia.core.treemodel.json.dependency.JsonDependencyNode;
import com.fit.cia.core.treemodel.json.dom.JsonNode;

import java.util.List;

public final class JsonWrapper {
	private final JsonNode jsonNode;
	private final List<JsonDependencyNode> dependencyList;

	JsonWrapper(JsonNode jsonNode, List<JsonDependencyNode> dependencyList) {
		this.jsonNode = jsonNode;
		this.dependencyList = dependencyList;
	}

	public final JsonNode getJsonNode() {
		return jsonNode;
	}

	public final List<JsonDependencyNode> getDependencyList() {
		return dependencyList;
	}
}
