package cia.cpp;

import cia.cpp.ast.INode;
import mrmathami.util.ImmutablePair;

import java.io.*;
import java.util.Set;

public final class ProjectVersionDifference implements Serializable {
	private static final long serialVersionUID = 3589355933372731081L;

	private final ProjectVersion versionA;
	private final ProjectVersion versionB;
	private final Set<INode> addedNodes;
	private final Set<ImmutablePair<INode, INode>> changedNodes;
	private final Set<ImmutablePair<INode, INode>> unchangedNodes;
	private final Set<INode> removedNodes;

	private ProjectVersionDifference(ProjectVersion versionA, ProjectVersion versionB, Set<INode> addedNodes, Set<ImmutablePair<INode, INode>> changedNodes, Set<ImmutablePair<INode, INode>> unchangedNodes, Set<INode> removedNodes) {
		this.versionA = versionA;
		this.versionB = versionB;
		this.addedNodes = addedNodes;
		this.changedNodes = changedNodes;
		this.unchangedNodes = unchangedNodes;
		this.removedNodes = removedNodes;
	}

	public static ProjectVersionDifference of(ProjectVersion versionA, ProjectVersion versionB, Set<INode> addedNodes, Set<ImmutablePair<INode, INode>> changedNodes, Set<ImmutablePair<INode, INode>> unchangedNodes, Set<INode> removedNodes) {
		return new ProjectVersionDifference(versionA, versionB, addedNodes, changedNodes, unchangedNodes, removedNodes);
	}

	public static ProjectVersionDifference fromInputStream(InputStream inputStream) throws IOException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try {
			return (ProjectVersionDifference) objectInputStream.readObject();
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new IOException("Wrong input file format!", e);
		}
	}

	public final void toOutputStream(OutputStream outputStream) throws IOException {
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
	}

	public final ProjectVersion getVersionA() {
		return versionA;
	}

	public final ProjectVersion getVersionB() {
		return versionB;
	}

	public final Set<INode> getAddedNodes() {
		return addedNodes;
	}

	public final Set<ImmutablePair<INode, INode>> getChangedNodes() {
		return changedNodes;
	}

	public final Set<ImmutablePair<INode, INode>> getUnchangedNodes() {
		return unchangedNodes;
	}

	public final Set<INode> getRemovedNodes() {
		return removedNodes;
	}
}
