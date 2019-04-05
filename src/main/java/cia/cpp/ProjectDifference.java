package cia.cpp;

import cia.cpp.ast.INode;
import mrmathami.util.ImmutablePair;

import java.util.Set;

public final class ProjectDifference {
	private final Set<INode> addedNodes;
	private final Set<ImmutablePair<INode, INode>> changedNodes;
	private final Set<INode> removedNodes;

	private ProjectDifference(Set<INode> addedNodes, Set<ImmutablePair<INode, INode>> changedNodes, Set<INode> removedNodes) {
		this.addedNodes = addedNodes;
		this.changedNodes = changedNodes;
		this.removedNodes = removedNodes;
	}

	public static ProjectDifference of(Set<INode> addedNodes, Set<ImmutablePair<INode, INode>> changedNodes, Set<INode> removedNodes) {
		return new ProjectDifference(addedNodes, changedNodes, removedNodes);
	}
}
