package cia.cpp.differ;

import cia.cpp.ProjectVersion;
import cia.cpp.VersionDifference;
import cia.cpp.ast.*;
import mrmathami.util.ImmutablePair;
import mrmathami.util.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class VersionDiffer {
	private VersionDiffer() {
	}

	public static VersionDifference compare(ProjectVersion versionA, ProjectVersion versionB) {
		final IRoot rootA = versionA.getRootNode();
		final IRoot rootB = versionB.getRootNode();

		final Map<INode, INode> nodeMapA = new HashMap<>();
		final Map<INode, INode> nodeMapB = new HashMap<>();
		for (final ITreeNode node : rootA) {
			final INode nodeA = INode.getNode(node);
			nodeMapA.put(nodeA, nodeA);
		}
		for (final ITreeNode node : rootB) {
			final INode nodeB = INode.getNode(node);
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
					if (!nodeA.equalsDependencies(nodeB)
							|| (nodeA instanceof IClass && !((IClass) nodeA).equalsBase(nodeB))) {
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

		// todo: dbg
		{
			try {
				try (final FileWriter fileWriter = new FileWriter("R:\\addedNodes.log")) {
					fileWriter.write(Utilities.collectionToString(addedNodes));
				}
				try (final FileWriter fileWriter = new FileWriter("R:\\changedNodes.log")) {
					fileWriter.write(Utilities.collectionToString(changedNodes));
				}
				try (final FileWriter fileWriter = new FileWriter("R:\\unchangedNodes.log")) {
					fileWriter.write(Utilities.collectionToString(unchangedNodes));
				}
				try (final FileWriter fileWriter = new FileWriter("R:\\removedNodes.log")) {
					fileWriter.write(Utilities.collectionToString(removedNodes));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return VersionDifference.of(versionA, versionB, addedNodes, changedNodes, unchangedNodes, removedNodes);
	}
}
