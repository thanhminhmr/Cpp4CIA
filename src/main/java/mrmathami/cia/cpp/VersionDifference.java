package mrmathami.cia.cpp;

import mrmathami.cia.cpp.ast.Node;
import mrmathami.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Set;

public final class VersionDifference implements Serializable {
	private static final long serialVersionUID = -7916436622963737241L;

	private final ProjectVersion versionA;
	private final ProjectVersion versionB;
	private final Set<Node> addedNodes;
	private final Set<Pair<Node, Node>> changedNodes;
	private final Set<Pair<Node, Node>> unchangedNodes;
	private final Set<Node> removedNodes;

	private VersionDifference(ProjectVersion versionA, ProjectVersion versionB, Set<Node> addedNodes, Set<Pair<Node, Node>> changedNodes, Set<Pair<Node, Node>> unchangedNodes, Set<Node> removedNodes) {
		this.versionA = versionA;
		this.versionB = versionB;
		this.addedNodes = Set.copyOf(addedNodes);
		this.changedNodes = Set.copyOf(changedNodes);
		this.unchangedNodes = Set.copyOf(unchangedNodes);
		this.removedNodes = Set.copyOf(removedNodes);
	}

	public static VersionDifference of(ProjectVersion versionA, ProjectVersion versionB, Set<Node> addedNodes, Set<Pair<Node, Node>> changedNodes, Set<Pair<Node, Node>> unchangedNodes, Set<Node> removedNodes) {
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

	public final Set<Node> getAddedNodes() {
		return addedNodes;
	}

	public final Set<Pair<Node, Node>> getChangedNodes() {
		return changedNodes;
	}

	public final Set<Pair<Node, Node>> getUnchangedNodes() {
		return unchangedNodes;
	}

	public final Set<Node> getRemovedNodes() {
		return removedNodes;
	}
}
