package cia.cpp.differ;

import cia.cpp.Project;
import cia.cpp.ProjectDifference;
import cia.cpp.ast.*;
import mrmathami.util.ImmutablePair;
import mrmathami.util.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ProjectDiffer {
	private ProjectDiffer() {
	}

	private static boolean isNodeEqual(INode nodeA, INode nodeB) {
		return nodeA.equals(nodeB) && nodeA.getDependencies().equals(nodeB.getDependencies());
	}

	public static ProjectDifference compare(Project projectA, Project projectB) {
		final IRoot rootA = projectA.getRootNode();
		final IRoot rootB = projectB.getRootNode();

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
		final Set<ImmutablePair<INode, INode>> changedNodes = new HashSet<>();
		final Set<INode> removedNodes = new HashSet<>();

		for (final INode nodeA : nodeMapA.keySet()) {
			if (!(nodeA instanceof IVariable && nodeA.getParent() instanceof IFunction)) {
				final INode nodeB = nodeMapB.get(nodeA);
				if (nodeB != null) {
					if (!nodeA.equalsDependencies(nodeB)
							|| (nodeA instanceof IClass && !((IClass) nodeA).equalsBase(nodeB))) {
						changedNodes.add(ImmutablePair.of(nodeA, nodeB));
					}
				} else {
					removedNodes.add(nodeA);
				}
			}
		}
		for (final INode nodeB : nodeMapB.keySet()) {
			if (!(nodeB instanceof IVariable && nodeB.getParent() instanceof IFunction)) {
				final INode nodeA = nodeMapA.get(nodeB);
				if (nodeA == null) {
					addedNodes.add(nodeB);
				}
			}
		}

		// todo: dbg
		{
			try (final FileWriter fileWriter = new FileWriter("R:\\addedNodes.log")) {
				fileWriter.write(Utilities.collectionToString(addedNodes));
			} catch (IOException e) {
				e.printStackTrace();
			}
			try (final FileWriter fileWriter = new FileWriter("R:\\changedNodes.log")) {
				fileWriter.write(Utilities.collectionToString(changedNodes));
			} catch (IOException e) {
				e.printStackTrace();
			}
			try (final FileWriter fileWriter = new FileWriter("R:\\removedNodes.log")) {
				fileWriter.write(Utilities.collectionToString(removedNodes));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ProjectDifference.of(projectA, projectB, addedNodes, changedNodes, removedNodes);
	}
}
