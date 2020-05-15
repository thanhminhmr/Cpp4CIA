package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.util.ThreadFactoryBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

final class ImpactWeightBuilder {
	@Nonnull private static final ExecutorService EXECUTOR_SERVICE =
			new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors(),
					1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
					new ThreadFactoryBuilder().setNamePrefix("ImpactWeightBuilder").setDaemon(true).build()
			);

	private ImpactWeightBuilder() {
	}

	@Nonnull
	private static Callable<double[]> createSingleCalculateTask(@Nonnull Map<DependencyType, Double> weightMap,
			@Nonnull RootNode rootNode, @Nonnull CppNode changedNode) {
		return new Callable<>() {
			private final int nodeCount = rootNode.getNodeCount();
			@Nonnull private final double[] weights = new double[nodeCount];
			@Nonnull private final BitSet pathSet = new BitSet(nodeCount);

			private void recursiveCalculate(@Nonnull CppNode currentNode, double currentWeight) {
				for (final CppNode nextNode : currentNode.getAllDependencyFrom()) {
					final int nextId = nextNode.getId();
					if (!pathSet.get(nextId)) {
						pathSet.set(nextId);

						double linkWeight = 1.0;
						for (final Map.Entry<DependencyType, Integer> entry : currentNode.getNodeDependencyFrom(nextNode).entrySet()) {
							linkWeight *= Math.pow(1.0 - weightMap.getOrDefault(entry.getKey(), 0.0), entry.getValue());
						}

						final double nextWeight = currentWeight * (1.0 - linkWeight);
						weights[nextId] *= 1.0 - nextWeight;
						recursiveCalculate(nextNode, nextWeight);

						pathSet.clear(nextId);
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
		final ArrayList<Callable<double[]>> tasks = new ArrayList<>(changedNodes.size());
		for (final CppNode node : changedNodes) tasks.add(createSingleCalculateTask(weightMap, rootNode, node));

		final int nodeCount = rootNode.getNodeCount();
		final double[] weights = new double[nodeCount];
		Arrays.fill(weights, 1.0f);

		try {
			final List<Future<double[]>> futures = EXECUTOR_SERVICE.invokeAll(tasks);
			for (final Future<double[]> future : futures) {
				final double[] singleWeights = future.get();
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
