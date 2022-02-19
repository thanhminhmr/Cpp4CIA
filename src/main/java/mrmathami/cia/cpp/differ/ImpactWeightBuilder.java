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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

final class ImpactWeightBuilder {
	private static final double THRESHOLD = 0x1.0p-128;

	@Nonnull private final List<CppNode> changedNodes;
	@Nonnull private final double[] changedWeights;
	@Nonnull private final BitSet changedPathSet;
	@Nonnull private final double[] dependencyWeights;
	private final int maxDepth;

	private ImpactWeightBuilder(@Nonnull List<CppNode> changedNodes, @Nonnull double[] changedWeights,
			@Nonnull BitSet changedPathSet, @Nonnull double[] dependencyWeights, int maxDepth) {
		this.changedNodes = changedNodes;
		this.changedWeights = changedWeights;
		this.changedPathSet = changedPathSet;
		this.dependencyWeights = dependencyWeights;
		this.maxDepth = maxDepth;
	}

	@Nonnull
	static double[] calculate(@Nonnull double[] dependencyWeights, @Nonnull RootNode rootNode,
			@Nonnull List<CppNode> changedNodes, int maxDepth) throws CppException {
		final int nodeCount = rootNode.getNodeCount();

		final double[] changedWeights = new double[nodeCount];
		if (changedNodes.isEmpty()) return changedWeights;

		final BitSet changedPathSet = new BitSet(nodeCount);
		Arrays.fill(changedWeights, 1.0);
		for (final CppNode changedNode : changedNodes) {
			final int changedId = changedNode.getId();
			changedWeights[changedId] = 0.0;
			changedPathSet.set(changedId);
		}

		return new ImpactWeightBuilder(changedNodes, changedWeights, changedPathSet, dependencyWeights, maxDepth)
				.build();
	}

	@Nonnull
	private double[] build() throws CppException {
		try {
			final double[] weights = ForkJoinPool.commonPool()
					.submit(new CalculationTask(0, changedNodes.size()))
					.get();
			for (int i = 0; i < weights.length; i++) {
				weights[i] = 1.0 - weights[i];
			}
			return weights;
		} catch (final InterruptedException | ExecutionException exception) {
			throw new CppException("Cannot calculate impactWeights!", exception);
		}
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

	private final class CalculationTask extends RecursiveTask<double[]> {
		private final int start;
		private final int length;

		CalculationTask(int start, int length) {
			this.start = start;
			this.length = length;
		}

		@Nonnull
		@Override
		protected double[] compute() {
			if (length > 1) {
				final int half = length >>> 1;
				final ForkJoinTask<double[]> taskA = new CalculationTask(start, half).fork();
				final ForkJoinTask<double[]> taskB = new CalculationTask(start + half, length - half).fork();
				return reduceWeights(taskA.join(), taskB.join());
			} else {
				return calculate(changedNodes.get(start));
			}
		}

		@Nonnull
		private double[] calculate(@Nonnull CppNode node) {
			return new Function<CppNode, double[]>() {
				@Nonnull private final double[] weights = changedWeights.clone();
				@Nonnull private final BitSet pathSet = (BitSet) changedPathSet.clone();

				private void recursiveCalculate(@Nonnull CppNode currentNode, double currentWeight, int depth) {
					for (final CppNode nextNode : currentNode.getAllDependencyFrom()) {
						final int nextId = nextNode.getId();
						if (pathSet.get(nextId)) continue;
						final DependencyMap dependencyMap = currentNode.getNodeDependencyFrom(nextNode);
						final double nextWeight = currentWeight * linkWeight(dependencyMap, dependencyWeights);
						weights[nextId] *= 1.0 - nextWeight;
						if (depth < maxDepth && nextWeight >= THRESHOLD) {
							pathSet.set(nextId);
							recursiveCalculate(nextNode, nextWeight, depth + 1);
							pathSet.clear(nextId);
						}
					}
				}

				@Nonnull
				@Override
				public double[] apply(@Nonnull CppNode changedNode) {
					//System.err.println("Thread " + Thread.currentThread().getId() + " START " + changedNode);

					recursiveCalculate(changedNode, 1.0, 0);

					//System.err.println("Thread " + Thread.currentThread().getId() + " END " + changedNode);
					return weights;
				}
			}.apply(node);
		}
	}
}
