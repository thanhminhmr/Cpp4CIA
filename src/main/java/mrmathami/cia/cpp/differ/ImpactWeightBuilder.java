package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.ast.DependencyMap;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.RootNode;

import mrmathami.annotations.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

final class ImpactWeightBuilder {
	private static final double THRESHOLD = 0x1.0p-256;

	private ImpactWeightBuilder() {
	}

	@Nonnull
	private static Callable<double[]> createSingleCalculateTask(@Nonnull double[] dependencyWeights,
			@Nonnull RootNode rootNode, @Nonnull CppNode changedNode) {

		return new Callable<>() {
			private final int nodeCount = rootNode.getNodeCount();
			@Nonnull private final double[] weights = new double[nodeCount];
			@Nonnull private final BitSet pathSet = new BitSet(nodeCount);

			private void recursiveCalculate(@Nonnull CppNode currentNode, double currentWeight) {
				for (final CppNode nextNode : currentNode.getAllDependencyFrom()) {
					final int nextId = nextNode.getId();
					if (!pathSet.get(nextId)) {
						if (weights[nextId] >= THRESHOLD) {
							pathSet.set(nextId);

							double linkWeight = 1.0;
							final DependencyMap dependencyMap = currentNode.getNodeDependencyFrom(nextNode);
							for (final DependencyType type : DependencyType.values) {
								linkWeight *= Math.pow(1.0 - dependencyWeights[type.ordinal()], dependencyMap.getCount(type));
							}

							final double nextWeight = currentWeight * (1.0 - linkWeight);
							weights[nextId] *= 1.0 - nextWeight;
							recursiveCalculate(nextNode, nextWeight);

							pathSet.clear(nextId);
						} else {
							weights[nextId] = 0.0;
						}
					}
				}
			}

			@Override
			public double[] call() {
				Arrays.fill(weights, 1.0);

				final int changedId = changedNode.getId();
				weights[changedId] = 0.0;
				pathSet.set(changedId);

				recursiveCalculate(changedNode, 1.0);

//				for (int i = 0; i < nodeCount; i++) weights[i] = 1.0f - weights[i]; // NOTE: change me both!!
				return weights;
			}
		};
	}

	@Nonnull
	static double[] calculate(@Nonnull Map<DependencyType, Double> weightMap, @Nonnull RootNode rootNode,
			@Nonnull List<CppNode> changedNodes) throws CppException {


		final double[] dependencyWeights = new double[DependencyType.values.size()];
		for (final Map.Entry<DependencyType, Double> entry : weightMap.entrySet()) {
			dependencyWeights[entry.getKey().ordinal()] = entry.getValue();
		}

		final ExecutorService executorService = Executors.newWorkStealingPool();
		final List<Future<double[]>> tasks = new ArrayList<>(changedNodes.size());
		for (final CppNode node : changedNodes) {
			tasks.add(executorService.submit(createSingleCalculateTask(dependencyWeights, rootNode, node)));
		}

		final int nodeCount = rootNode.getNodeCount();
		final double[] weights = new double[nodeCount];
		Arrays.fill(weights, 1.0f);

		try {
			for (final Future<double[]> task : tasks) {
				final double[] singleWeights = task.get();
//				for (int i = 0; i < nodeCount; i++) weights[i] *= 1.0f - singleWeights[i]; // NOTE: change me both!!
				for (int i = 0; i < nodeCount; i++) weights[i] *= singleWeights[i];
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new CppException("Cannot calculate impactWeights!", e);
		}

		for (int i = 0; i < nodeCount; i++) weights[i] = 1.0f - weights[i];
		return weights;
	}
}
