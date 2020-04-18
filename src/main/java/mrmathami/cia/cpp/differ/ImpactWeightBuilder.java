package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ast.Node;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.util.ThreadFactoryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
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
	private static ImpactPaths getOrCreateImpactPaths(@Nonnull ImpactPaths[] list, @Nonnull Node node) {
		final int nodeId = node.getId();
		final ImpactPaths paths = list[nodeId];
		if (paths != null) return paths;
		final ImpactPaths newValue = ImpactPaths.of(node);
		list[nodeId] = newValue;
		return newValue;
	}

	@Nonnull
	private static Callable<float[]> createSingleCalculateTask(@Nonnull RootNode rootNode, @Nonnull Node changedNode) {
		return () -> {
			// todo: should we use LinkedHashSet for O(1) tracing? Or just simple linked pointer with O(n) contains check would be enough?
			final int nodeCount = rootNode.getNodeCount();
			final ImpactPaths[] pathLists = new ImpactPaths[nodeCount];
			final Queue<Node> queue = new LinkedList<>();
			pathLists[changedNode.getId()] = ImpactPaths.ofSingle(changedNode, ImpactPath.start(changedNode));
			queue.add(changedNode);

			while (!queue.isEmpty()) {
				final Node node = queue.poll();
				final ImpactPaths paths = pathLists[node.getId()];
				for (final Node nextNode : node.getAllDependencyFrom()) {
					final ImpactPaths nextPaths = getOrCreateImpactPaths(pathLists, nextNode);
					for (final ImpactPath path : paths) {
						if (!path.contains(nextNode)) {
							/* TODO: use a proper value for linkWeight */
							nextPaths.add(ImpactPath.next(path, nextNode, 0.2f));
							queue.add(nextNode);
						}
					}
				}
			}

			final float[] weights = new float[nodeCount];
			for (final ImpactPaths paths : pathLists) {
				final Node node = paths.currentNode;
				final int nodeId = node.getId();
				float weight = 1.0f;
				for (final ImpactPath path : paths) weight *= 1.0f - path.pathWeight;
				weights[nodeId] = 1.0f - weight;
			}
			return weights;
		};
	}

	@Nonnull
	static float[] calculateWeights(@Nonnull RootNode rootNode, @Nonnull List<Node> changedNodes) throws CppException {
		final ArrayList<Callable<float[]>> tasks = new ArrayList<>(changedNodes.size());
		for (final Node node : changedNodes) tasks.add(createSingleCalculateTask(rootNode, node));

		final int nodeCount = rootNode.getNodeCount();
		final float[] weights = new float[nodeCount];
		Arrays.fill(weights, 1.0f);

		try {
			final List<Future<float[]>> futures = EXECUTOR_SERVICE.invokeAll(tasks);
			for (final Future<float[]> future : futures) {
				final float[] singleWeights = future.get();
				for (int i = 0; i < nodeCount; i++) weights[i] *= 1.0f - singleWeights[i];
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new CppException("Cannot calculate impactWeights!", e);
		}

		for (int i = 0; i < nodeCount; i++) weights[i] = 1.0f - weights[i];
		return weights;
	}

	private static final class ImpactPath extends LinkedHashSet<Node> {
		private final float pathWeight;

		private ImpactPath(@Nonnull Node start) {
			this.pathWeight = 1.0f;
			add(start);
		}

		private ImpactPath(@Nonnull ImpactPath path, @Nonnull Node next, float pathWeight) {
			super(path);
			this.pathWeight = pathWeight;
			add(next);
		}

		@Nonnull
		private static ImpactPath start(@Nonnull Node start) {
			return new ImpactPath(start);
		}

		@Nullable
		private static ImpactPath next(@Nonnull ImpactPath path, @Nonnull Node next, float linkWeight) {
			return path.contains(next) ? null : new ImpactPath(path, next, path.pathWeight * linkWeight);
		}
	}

	private static final class ImpactPaths extends LinkedList<ImpactPath> {
		@Nonnull private final Node currentNode;

		private ImpactPaths(@Nonnull Node currentNode) {
			this.currentNode = currentNode;
		}

		@Nonnull
		private static ImpactPaths of(@Nonnull Node currentNode) {
			return new ImpactPaths(currentNode);
		}

		@Nonnull
		private static ImpactPaths ofSingle(@Nonnull Node currentNode, @Nonnull ImpactPath path) {
			final ImpactPaths paths = new ImpactPaths(currentNode);
			paths.add(path);
			return paths;
		}
	}
}
