package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.IntegralNode;
import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.cia.cpp.builder.ProjectVersion;
import mrmathami.util.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VersionDiffer {
	@Nonnull public static final Map<DependencyType, Double> IMPACT_WEIGHT_MAP = Map.of(
			DependencyType.USE, 0.8495204,
			DependencyType.MEMBER, 0.7816402,
			DependencyType.INHERITANCE, 0.7071755,
			DependencyType.INVOCATION, 0.7487174,
			DependencyType.OVERRIDE, 0.7128108
	);

	private VersionDiffer() {
	}

	@Nonnull
	public static VersionDifference compare(@Nonnull ProjectVersion versionA, @Nonnull ProjectVersion versionB,
			@Nonnull Map<DependencyType, Double> dependencyTypeImpactWeightMap) throws CppException {
		final RootNode rootA = versionA.getRootNode();
		final RootNode rootB = versionB.getRootNode();

		final CppNode.Matcher matcher = new CppNode.Matcher();

		final Map<CppNode.Wrapper, CppNode> nodeMapA = new HashMap<>();
		final Map<CppNode.Wrapper, CppNode> nodeMapB = new HashMap<>();
		nodeMapA.put(new CppNode.Wrapper(rootA, CppNode.MatchLevel.SIMILAR, matcher), rootA);
		nodeMapB.put(new CppNode.Wrapper(rootB, CppNode.MatchLevel.SIMILAR, matcher), rootB);
		for (final CppNode nodeA : rootA) {
			if (!(nodeA instanceof IntegralNode)) {
				nodeMapA.put(new CppNode.Wrapper(nodeA, CppNode.MatchLevel.SIMILAR, matcher), nodeA);
			}
		}
		for (final CppNode nodeB : rootB) {
			if (!(nodeB instanceof IntegralNode)) {
				nodeMapB.put(new CppNode.Wrapper(nodeB, CppNode.MatchLevel.SIMILAR, matcher), nodeB);
			}
		}

		final Set<CppNode> addedNodes = new HashSet<>();
		final Set<Pair<CppNode, CppNode>> changedNodes = new HashSet<>();
		final Set<Pair<CppNode, CppNode>> unchangedNodes = new HashSet<>();
		final Set<CppNode> removedNodes = new HashSet<>();

		final List<CppNode> changedListB = new LinkedList<>();

		for (final CppNode.Wrapper wrapperA : nodeMapA.keySet()) {
			final CppNode nodeA = wrapperA.getNode();
			final CppNode nodeB = nodeMapB.get(wrapperA);
			if (nodeB != null) {
				if (matcher.isNodeMatch(nodeA, nodeB, CppNode.MatchLevel.IDENTICAL)) {
					unchangedNodes.add(Pair.immutableOf(nodeA, nodeB));
				} else {
					changedNodes.add(Pair.immutableOf(nodeA, nodeB));
					changedListB.add(nodeB);
				}
			} else {
				removedNodes.add(nodeA);
			}
		}
		for (final CppNode.Wrapper wrapperB : nodeMapB.keySet()) {
			final CppNode nodeA = nodeMapA.get(wrapperB);
			final CppNode nodeB = wrapperB.getNode();
			if (nodeA == null) {
				addedNodes.add(nodeB);
				changedListB.add(nodeB);
			}
		}

		final DependencyType[] types = DependencyType.values();
		final double[] typeImpactWeights = new double[types.length];
		for (final DependencyType type : types) {
			typeImpactWeights[type.ordinal()] = dependencyTypeImpactWeightMap.get(type);
		}

		final double[] impactWeights = ImpactWeightBuilder.calculate(dependencyTypeImpactWeightMap, rootB, changedListB);

		return new VersionDifference(versionA, versionB, addedNodes, changedNodes, unchangedNodes, removedNodes, typeImpactWeights, impactWeights);
	}
}
