package cia.cpp.differ;

import cia.cpp.Project;
import cia.cpp.ProjectDifference;
import cia.cpp.ast.INode;
import cia.cpp.ast.IRoot;
import cia.cpp.ast.ITreeNode;
import mrmathami.util.ImmutablePair;
import mrmathami.util.Pair;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
			final INode nodeB = nodeMapB.get(nodeA);
			if (nodeB != null) {
				if (!nodeA.equals(nodeB)) {
					throw new IllegalStateException();
				}
				if (!nodeA.getDependencies().equals(nodeB.getDependencies())) {
					changedNodes.add(ImmutablePair.of(nodeA, nodeB));
				}
			} else {
				removedNodes.add(nodeA);
			}
		}
		for (final INode nodeB : nodeMapB.keySet()) {
			final INode nodeA = nodeMapA.get(nodeB);
			if (nodeA == null) addedNodes.add(nodeB);
			else
			if (!nodeA.equals(nodeB)) {
				throw new IllegalStateException();
			}
		}

		// todo: dbg
		{
			try (final FileWriter fileWriter = new FileWriter("R:\\addedNodes.log")) {
				fileWriter.write(addedNodes.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try (final FileWriter fileWriter = new FileWriter("R:\\changedNodes.log")) {
				fileWriter.write(changedNodes.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try (final FileWriter fileWriter = new FileWriter("R:\\removedNodes.log")) {
				fileWriter.write(removedNodes.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ProjectDifference.of(addedNodes, changedNodes, removedNodes);
	}
}
