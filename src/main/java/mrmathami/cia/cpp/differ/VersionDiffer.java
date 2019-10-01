package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.ProjectVersion;
import mrmathami.cia.cpp.VersionDifference;
import mrmathami.cia.cpp.ast.IntegralNode;
import mrmathami.cia.cpp.ast.Node;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.util.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class VersionDiffer {
	private VersionDiffer() {
	}

	public static VersionDifference compare(ProjectVersion versionA, ProjectVersion versionB, VersionDifferDebugger debugger) {
		final RootNode rootA = versionA.getRootNode();
		final RootNode rootB = versionB.getRootNode();

		final Node.Matcher matcher = new Node.Matcher();

		final Map<Node.Wrapper, Node> nodeMapA = new HashMap<>();
		final Map<Node.Wrapper, Node> nodeMapB = new HashMap<>();
		for (final Node nodeA : rootA) {
			if (!(nodeA instanceof IntegralNode)) {
				nodeMapA.put(new Node.Wrapper(nodeA, Node.MatchLevel.SIMILAR, matcher), nodeA);
			}
		}
		for (final Node nodeB : rootB) {
			if (!(nodeB instanceof IntegralNode)) {
				nodeMapB.put(new Node.Wrapper(nodeB, Node.MatchLevel.SIMILAR, matcher), nodeB);
			}
		}
		nodeMapA.put(new Node.Wrapper(rootA, Node.MatchLevel.SIMILAR, matcher), rootA);
		nodeMapB.put(new Node.Wrapper(rootB, Node.MatchLevel.SIMILAR, matcher), rootB);

		final Set<Node> addedNodes = new HashSet<>();
		final Set<Pair<Node, Node>> changedNodes = new HashSet<>();
		final Set<Pair<Node, Node>> unchangedNodes = new HashSet<>();
		final Set<Node> removedNodes = new HashSet<>();

		for (final Node.Wrapper wrapperA : nodeMapA.keySet()) {
			final Node nodeA = wrapperA.getNode();
			final Node nodeB = nodeMapB.get(wrapperA);
			if (nodeB != null) {
				if (!matcher.isNodeMatch(nodeA, nodeB, Node.MatchLevel.IDENTICAL)) {
					changedNodes.add(Pair.immutableOf(nodeA, nodeB));
				} else {
					unchangedNodes.add(Pair.immutableOf(nodeA, nodeB));
				}
			} else {
				removedNodes.add(nodeA);
			}
		}
		for (final Node.Wrapper wrapperB : nodeMapB.keySet()) {
			final Node nodeA = nodeMapA.get(wrapperB);
			final Node nodeB = wrapperB.getNode();
			if (nodeA == null) {
				addedNodes.add(nodeB);
			}
		}

		if (debugger != null) {
			debugger.setVersionDifferenceName(versionA.getVersionName() + "-" + versionB.getVersionName());
			debugger.setAddedNodes(addedNodes);
			debugger.setRemovedNodes(removedNodes);
			debugger.setChangedNodes(changedNodes);
			debugger.setUnchangedNodes(unchangedNodes);
		}

		return VersionDifference.of(versionA, versionB, addedNodes, changedNodes, unchangedNodes, removedNodes);
	}
}
