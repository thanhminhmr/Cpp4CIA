package mrmathami.cia.cpp.differ;

import mrmathami.annotations.Nonnull;
import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.ast.DependencyMap;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.RootNode;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

final class ImpactWeightBuilder {
	private static final double THRESHOLD = 0x1.0p-128;

	private ImpactWeightBuilder() {
	}

	@Nonnull
	static double[] calculate(@Nonnull double[] dependencyWeights, @Nonnull RootNode rootNode,
			@Nonnull List<CppNode> changedNodes) throws CppException {
		final int nodeCount = rootNode.getNodeCount();

		final double[] changedWeights = new double[nodeCount];
		final BitSet changedPathSet = new BitSet(nodeCount);
		Arrays.fill(changedWeights, 1.0);
		for (final CppNode changedNode : changedNodes) {
			final int changedId = changedNode.getId();
			changedWeights[changedId] = 0.0;
			changedPathSet.set(changedId);
		}

		final Optional<double[]> weightsOptional = changedNodes.parallelStream().unordered()
				.map(node -> new Function<CppNode, double[]>() {
					@Nonnull private final double[] weights = changedWeights.clone();
					@Nonnull private final BitSet pathSet = (BitSet) changedPathSet.clone();

					private void recursiveCalculate(@Nonnull CppNode currentNode, double currentWeight) {
						for (final CppNode nextNode : currentNode.getAllDependencyFrom()) {
							final int nextId = nextNode.getId();
							if (pathSet.get(nextId)) continue;
							final DependencyMap dependencyMap = currentNode.getNodeDependencyFrom(nextNode);
							final double nextWeight = currentWeight * linkWeight(dependencyMap, dependencyWeights);
							weights[nextId] *= 1.0 - nextWeight;
							if (nextWeight >= THRESHOLD) {
								pathSet.set(nextId);
								recursiveCalculate(nextNode, nextWeight);
								pathSet.clear(nextId);
							}
						}
					}

					@Nonnull
					@Override
					public double[] apply(@Nonnull CppNode changedNode) {
						System.err.println("Thread " + Thread.currentThread().getId() + " START " + changedNode);

						recursiveCalculate(changedNode, 1.0);

						System.err.println("Thread " + Thread.currentThread().getId() + " END " + changedNode);
						return weights;
					}
				}.apply(node))
				.reduce(ImpactWeightBuilder::reduceWeights);
		if (weightsOptional.isEmpty()) {
			throw new CppException("Cannot calculate impactWeights!");
		}
		final double[] weights = weightsOptional.get();
		assert weights.length == nodeCount;
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 1.0 - weights[i];
		}
		return weights;
	}

	private static double linkWeight(@Nonnull DependencyMap dependencyMap, @Nonnull double[] dependencyWeights) {
		double linkWeight = 1.0;
		for (final DependencyType type : DependencyType.values) {
			linkWeight *= Math.pow(1.0 - dependencyWeights[type.ordinal()], dependencyMap.getCount(type));
		}
		return 1.0 - linkWeight;
	}

	@Nonnull
	private static double[] reduceWeights(@Nonnull double[] weightsA, @Nonnull double[] weightsB) {
		assert weightsA.length == weightsB.length;
		for (int i = 0; i < weightsA.length; i++) {
			weightsA[i] *= weightsB[i];
		}
		return weightsA;
	}
}
