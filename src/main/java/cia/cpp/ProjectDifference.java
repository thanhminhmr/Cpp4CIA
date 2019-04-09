package cia.cpp;

import cia.cpp.ast.INode;
import mrmathami.util.ImmutablePair;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Set;

public final class ProjectDifference implements Serializable {
	private static final long serialVersionUID = -6153257141723279948L;

	private final Project projectA;
	private final Project projectB;
	private final Set<INode> addedNodes;
	private final Set<ImmutablePair<INode, INode>> changedNodes;
	private final Set<INode> removedNodes;

	private ProjectDifference(Project projectA, Project projectB, Set<INode> addedNodes, Set<ImmutablePair<INode, INode>> changedNodes, Set<INode> removedNodes) {
		this.projectA = projectA;
		this.projectB = projectB;
		this.addedNodes = addedNodes;
		this.changedNodes = changedNodes;
		this.removedNodes = removedNodes;
	}

	public static ProjectDifference of(Project projectA, Project projectB, Set<INode> addedNodes, Set<ImmutablePair<INode, INode>> changedNodes, Set<INode> removedNodes) {
		return new ProjectDifference(projectA, projectB, addedNodes, changedNodes, removedNodes);
	}

	public static ProjectDifference fromInputStream(@Nonnull InputStream inputStream) throws IOException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try {
			return (ProjectDifference) objectInputStream.readObject();
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new IOException("Wrong input file format!", e);
		}
	}

	public final void toOutputStream(@Nonnull OutputStream outputStream) throws IOException {
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
	}

	public Project getProjectA() {
		return projectA;
	}

	public Project getProjectB() {
		return projectB;
	}

	public Set<INode> getAddedNodes() {
		return addedNodes;
	}

	public Set<ImmutablePair<INode, INode>> getChangedNodes() {
		return changedNodes;
	}

	public Set<INode> getRemovedNodes() {
		return removedNodes;
	}
}
