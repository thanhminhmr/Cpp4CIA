package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class RootNode extends Node implements IRoot {
	private static final long serialVersionUID = -6762481249107378559L;

	private RootNode() {
		super("ROOT", "ROOT", "ROOT");
	}

	@Nonnull
	public static IRootBuilder builder() {
		return new RootNodeBuilder();
	}

	@Override
	public void calculateDistance(@Nonnull Set<INode> changeSet) {
		setDistance(Float.MAX_VALUE);
		for (final INode node : this) node.setDistance(Float.MAX_VALUE);
		for (final INode node : changeSet) node.setDistance(0.0f);

		final Queue<INode> calculatedQueue = new LinkedList<>(changeSet);
		while (calculatedQueue.peek() != null) {
			final INode node = calculatedQueue.poll();
			final List<INode> dependencyFrom = node.getAllDependencyFrom();
			for (final INode fromNode : dependencyFrom) {
				float weigth = 0.0f;
				for (Map.Entry<DependencyType, Integer> entry : node.getNodeDependencyFrom(fromNode).entrySet()) {
					weigth += entry.getKey().getBackwardWeight() * entry.getValue();
				}
				if (weigth > 0.0f) {
					float distance = node.getDistance() + 1.0f / weigth;
					if (distance < fromNode.getDistance()) {
						fromNode.setDistance(distance);
						calculatedQueue.add(fromNode);
					}
				}
			}
		}
	}

	@Nonnull
	@Override
	public final List<IIntegral> getIntegrals() {
		return getChildrenList(IIntegral.class);
	}

	@Nonnull
	@Override
	public final List<IClass> getClasses() {
		return getChildrenList(IClass.class);
	}

	@Nonnull
	@Override
	public final List<IEnum> getEnums() {
		return getChildrenList(IEnum.class);
	}

	@Nonnull
	@Override
	public final List<IFunction> getFunctions() {
		return getChildrenList(IFunction.class);
	}

	@Nonnull
	@Override
	public final List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}

	public static final class RootNodeBuilder extends NodeBuilder<IRoot, IRootBuilder> implements IRootBuilder {
		private RootNodeBuilder() {
			setName("ROOT");
			setUniqueName("ROOT");
			setSignature("ROOT");
		}

		@Nonnull
		@Override
		public final IRoot build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			return new RootNode();
		}
	}
}
