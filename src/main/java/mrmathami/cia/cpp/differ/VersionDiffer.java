package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.ProjectVersion;
import mrmathami.cia.cpp.VersionDifference;
import mrmathami.cia.cpp.ast.IClass;
import mrmathami.cia.cpp.ast.IIntegral;
import mrmathami.cia.cpp.ast.INode;
import mrmathami.cia.cpp.ast.IRoot;
import mrmathami.util.ImmutablePair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class VersionDiffer {
	private VersionDiffer() {
	}

	public static VersionDifference compare(ProjectVersion versionA, ProjectVersion versionB, VersionDifferDebugger debugger) {
		final IRoot rootA = versionA.getRootNode();
		final IRoot rootB = versionB.getRootNode();

		final Map<INode, INode> nodeMapA = new HashMap<>();
		final Map<INode, INode> nodeMapB = new HashMap<>();
		for (final INode nodeA : rootA) {
			nodeMapA.put(nodeA, nodeA);
		}
		for (final INode nodeB : rootB) {
			nodeMapB.put(nodeB, nodeB);
		}

		final Set<INode> addedNodes = new HashSet<>();
		final Set<Map.Entry<INode, INode>> changedNodes = new HashSet<>();
		final Set<Map.Entry<INode, INode>> unchangedNodes = new HashSet<>();
		final Set<INode> removedNodes = new HashSet<>();

		for (final INode nodeA : nodeMapA.keySet()) {
			if (!(nodeA instanceof IIntegral)/* && !(nodeA instanceof IVariable && nodeA.getParent() instanceof IFunction)*/) {
				final INode nodeB = nodeMapB.get(nodeA);
				if (nodeB != null) {
					if (!nodeA.equalsAllDependencyTo(nodeB)
							|| (nodeA instanceof IClass && !((IClass) nodeA).getBases().equals(((IClass) nodeB).getBases()))) {
						changedNodes.add(ImmutablePair.of(nodeA, nodeB));
					} else {
						unchangedNodes.add(ImmutablePair.of(nodeA, nodeB));
					}
				} else {
					removedNodes.add(nodeA);
				}
			}
		}
		for (final INode nodeB : nodeMapB.keySet()) {
			if (!(nodeB instanceof IIntegral)/* && !(nodeB instanceof IVariable && nodeB.getParent() instanceof IFunction)*/) {
				final INode nodeA = nodeMapA.get(nodeB);
				if (nodeA == null) {
					addedNodes.add(nodeB);
				}
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
