package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ProjectVersion;
import mrmathami.cia.cpp.VersionDifference;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.IntegralNode;
import mrmathami.cia.cpp.ast.Node;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.util.Pair;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VersionDiffer {
	@Nonnull public static final Map<DependencyType, Double> WEIGHT_MAP = Map.of(
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
			@Nonnull Map<DependencyType, Double> weightMap) throws CppException {
		final RootNode rootA = versionA.getRootNode();
		final RootNode rootB = versionB.getRootNode();

		final Node.Matcher matcher = new Node.Matcher();

		final Map<Node.Wrapper, Node> nodeMapA = new HashMap<>();
		final Map<Node.Wrapper, Node> nodeMapB = new HashMap<>();
		nodeMapA.put(new Node.Wrapper(rootA, Node.MatchLevel.SIMILAR, matcher), rootA);
		nodeMapB.put(new Node.Wrapper(rootB, Node.MatchLevel.SIMILAR, matcher), rootB);
		for (final Node nodeA : rootA) {
			if (!(nodeA instanceof IntegralNode)) {
				nodeMapA.put(new Node.Wrapper(nodeA, Node.MatchLevel.SIMILAR, matcher), nodeA);
			}
		}
		for (final Node nodeB : rootB) {
			if (!(nodeB instanceof IntegralNode)) {
				nodeMapB.put(new Node.Wrapper(nodeB, Node.MatchLevel.SIMILAR, matcher), nodeB);
			}
		}

		final Set<Node> addedNodes = new HashSet<>();
		final Set<Pair<Node, Node>> changedNodes = new HashSet<>();
		final Set<Pair<Node, Node>> unchangedNodes = new HashSet<>();
		final Set<Node> removedNodes = new HashSet<>();

		final List<Node> changedListB = new LinkedList<>();

		for (final Node.Wrapper wrapperA : nodeMapA.keySet()) {
			final Node nodeA = wrapperA.getNode();
			final Node nodeB = nodeMapB.get(wrapperA);
			if (nodeB != null) {
				if (matcher.isNodeMatch(nodeA, nodeB, Node.MatchLevel.IDENTICAL)) {
					unchangedNodes.add(Pair.immutableOf(nodeA, nodeB));
				} else {
					changedNodes.add(Pair.immutableOf(nodeA, nodeB));
					changedListB.add(nodeB);
				}
			} else {
				removedNodes.add(nodeA);
			}
		}
		for (final Node.Wrapper wrapperB : nodeMapB.keySet()) {
			final Node nodeA = nodeMapA.get(wrapperB);
			final Node nodeB = wrapperB.getNode();
			if (nodeA == null) {
				addedNodes.add(nodeB);
				changedListB.add(nodeB);
			}
		}

		final double[] weights = ImpactWeightBuilder.calculate(weightMap, rootB, changedListB);

		return VersionDifference.of(versionA, versionB, addedNodes, changedNodes, unchangedNodes, removedNodes, weights);
	}
}
