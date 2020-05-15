package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.cia.cpp.builder.ProjectVersion;
import mrmathami.util.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

public final class VersionDifference implements Serializable {
	private static final long serialVersionUID = 5201230926401028541L;

	@Nonnull private final ProjectVersion versionA;
	@Nonnull private final ProjectVersion versionB;
	@Nonnull private final Set<CppNode> addedNodes;
	@Nonnull private final Set<Pair<CppNode, CppNode>> changedNodes;
	@Nonnull private final Set<Pair<CppNode, CppNode>> unchangedNodes;
	@Nonnull private final Set<CppNode> removedNodes;
	@Nonnull private final double[] typeImpactWeights;
	@Nonnull private final double[] impactWeights;

	@Nullable private transient Map<DependencyType, Double> typeImpactWeightMap;
	@Nullable private transient Map<CppNode, Double> impactWeightMap;

	VersionDifference(@Nonnull ProjectVersion versionA, @Nonnull ProjectVersion versionB,
			@Nonnull Set<CppNode> addedNodes, @Nonnull Set<Pair<CppNode, CppNode>> changedNodes,
			@Nonnull Set<Pair<CppNode, CppNode>> unchangedNodes, @Nonnull Set<CppNode> removedNodes,
			@Nonnull double[] typeImpactWeights, @Nonnull double[] impactWeights) {
		this.versionA = versionA;
		this.versionB = versionB;
		this.addedNodes = Set.copyOf(addedNodes);
		this.changedNodes = Set.copyOf(changedNodes);
		this.unchangedNodes = Set.copyOf(unchangedNodes);
		this.removedNodes = Set.copyOf(removedNodes);
		this.typeImpactWeights = typeImpactWeights.clone();
		this.impactWeights = impactWeights.clone();
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
	public final Set<CppNode> getAddedNodes() {
		return addedNodes;
	}

	@Nonnull
	public final Set<Pair<CppNode, CppNode>> getChangedNodes() {
		return changedNodes;
	}

	@Nonnull
	public final Set<Pair<CppNode, CppNode>> getUnchangedNodes() {
		return unchangedNodes;
	}

	@Nonnull
	public final Set<CppNode> getRemovedNodes() {
		return removedNodes;
	}

	@Nonnull
	public final Map<DependencyType, Double> getDependencyTypeImpactWeightMap() {
		if (typeImpactWeightMap != null) return typeImpactWeightMap;
		final Map<DependencyType, Double> map = new EnumMap<>(DependencyType.class);
		for (final DependencyType type : DependencyType.values()) {
			map.put(type, typeImpactWeights[type.ordinal()]);
		}
		return this.typeImpactWeightMap = Map.copyOf(map);
	}

	@Nonnull
	public final Map<CppNode, Double> getImpactWeightMap() {
		if (impactWeightMap != null) return impactWeightMap;
		final Map<CppNode, Double> map = new IdentityHashMap<>();
		final RootNode rootNode = versionB.getRootNode();
		map.put(rootNode, impactWeights[0]); // root id == 0
		for (final CppNode node : rootNode) map.put(node, impactWeights[node.getId()]);
		return this.impactWeightMap = Map.copyOf(map);
	}
}
