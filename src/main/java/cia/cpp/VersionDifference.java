package cia.cpp;

import cia.cpp.ast.INode;
import mrmathami.util.Utilities;

import java.io.*;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public final class VersionDifference implements Serializable {
	private static final long serialVersionUID = 5005107530392882555L;

	private final ProjectVersion versionA;
	private final ProjectVersion versionB;
	private final Set<INode> addedNodes;
	private final Set<Map.Entry<INode, INode>> changedNodes;
	private final Set<Map.Entry<INode, INode>> unchangedNodes;
	private final Set<INode> removedNodes;

	private VersionDifference(ProjectVersion versionA, ProjectVersion versionB, Set<INode> addedNodes, Set<Map.Entry<INode, INode>> changedNodes, Set<Map.Entry<INode, INode>> unchangedNodes, Set<INode> removedNodes) {
		this.versionA = versionA;
		this.versionB = versionB;
		this.addedNodes = Set.copyOf(addedNodes);
		this.changedNodes = Set.copyOf(changedNodes);
		this.unchangedNodes = Set.copyOf(unchangedNodes);
		this.removedNodes = Set.copyOf(removedNodes);
	}

	public static VersionDifference of(ProjectVersion versionA, ProjectVersion versionB, Set<INode> addedNodes, Set<Map.Entry<INode, INode>> changedNodes, Set<Map.Entry<INode, INode>> unchangedNodes, Set<INode> removedNodes) {
		return new VersionDifference(versionA, versionB, addedNodes, changedNodes, unchangedNodes, removedNodes);
	}

	public static VersionDifference fromInputStream(InputStream inputStream) throws IOException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try {
			return (VersionDifference) objectInputStream.readObject();
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

	public final Set<Map.Entry<INode, INode>> getChangedNodes() {
		return changedNodes;
	}

	public final Set<Map.Entry<INode, INode>> getUnchangedNodes() {
		return unchangedNodes;
	}

	public final Set<INode> getRemovedNodes() {
		return removedNodes;
	}
}