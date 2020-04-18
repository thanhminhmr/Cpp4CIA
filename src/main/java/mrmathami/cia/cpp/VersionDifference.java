package mrmathami.cia.cpp;

import mrmathami.cia.cpp.ast.Node;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.util.Pair;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class VersionDifference implements Serializable {
	private static final long serialVersionUID = 3954976070986020324L;

	@Nonnull private final ProjectVersion versionA;
	@Nonnull private final ProjectVersion versionB;
	@Nonnull private final Set<Node> addedNodes;
	@Nonnull private final Set<Pair<Node, Node>> changedNodes;
	@Nonnull private final Set<Pair<Node, Node>> unchangedNodes;
	@Nonnull private final Set<Node> removedNodes;
	@Nonnull private final double[] impactWeights;

	private VersionDifference(@Nonnull ProjectVersion versionA, @Nonnull ProjectVersion versionB,
			@Nonnull Set<Node> addedNodes, @Nonnull Set<Pair<Node, Node>> changedNodes,
			@Nonnull Set<Pair<Node, Node>> unchangedNodes, @Nonnull Set<Node> removedNodes,
			@Nonnull double[] impactWeights) {
		this.versionA = versionA;
		this.versionB = versionB;
		this.addedNodes = Set.copyOf(addedNodes);
		this.changedNodes = Set.copyOf(changedNodes);
		this.unchangedNodes = Set.copyOf(unchangedNodes);
		this.removedNodes = Set.copyOf(removedNodes);
		this.impactWeights = impactWeights.clone();
	}

	@Nonnull
	public static VersionDifference of(@Nonnull ProjectVersion versionA, @Nonnull ProjectVersion versionB,
			@Nonnull Set<Node> addedNodes, @Nonnull Set<Pair<Node, Node>> changedNodes,
			@Nonnull Set<Pair<Node, Node>> unchangedNodes, @Nonnull Set<Node> removedNodes,
			@Nonnull double[] impactWeights) {
		return new VersionDifference(versionA, versionB, addedNodes, changedNodes, unchangedNodes, removedNodes, impactWeights);
	}

	@Nonnull
	public static VersionDifference fromInputStream(@Nonnull InputStream inputStream) throws IOException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try {
			return (VersionDifference) objectInputStream.readObject();
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new IOException("Wrong input file format!", e);
		}
	}

	public final void toOutputStream(@Nonnull OutputStream outputStream) throws IOException {
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
	}

	@Nonnull
	public final ProjectVersion getVersionA() {
		return versionA;
	}

	@Nonnull
	public final ProjectVersion getVersionB() {
		return versionB;
	}

	@Nonnull
	public final Set<Node> getAddedNodes() {
		return addedNodes;
	}

	@Nonnull
	public final Set<Pair<Node, Node>> getChangedNodes() {
		return changedNodes;
	}

	@Nonnull
	public final Set<Pair<Node, Node>> getUnchangedNodes() {
		return unchangedNodes;
	}

	@Nonnull
	public final Set<Node> getRemovedNodes() {
		return removedNodes;
	}

	@Nonnull
	public final Map<Node, Double> getImpactMap() {
		final Map<Node, Double> map = new IdentityHashMap<>();
		final RootNode rootNode = versionB.getRootNode();
		map.put(rootNode, impactWeights[0]); // root id == 0
		for (final Node node : rootNode) map.put(node, impactWeights[node.getId()]);
		return map;
	}
}
