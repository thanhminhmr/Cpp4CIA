package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class RootNode extends Node implements IRoot {
	private static final long serialVersionUID = 1509940265258487954L;

	private RootNode() {
		super("ROOT", "ROOT", "ROOT");
	}

	@Nonnull
	public static IRootBuilder builder() {
		return new RootNodeBuilder();
	}

	@Override
	public void calculateImpact(@Nonnull Set<INode> changeSet) {
		setImpact(Float.POSITIVE_INFINITY);
		for (final INode node : this) node.setImpact(Float.POSITIVE_INFINITY);
		for (final INode node : changeSet) node.setImpact(0.0f);

		final Queue<INode> calculatedQueue = new LinkedList<>(changeSet);
		while (calculatedQueue.peek() != null) {
			final INode node = calculatedQueue.poll();
			final List<INode> dependencyFrom = node.getAllDependencyFrom();
			for (final INode fromNode : dependencyFrom) {
				float weight = 0.0f;
				for (Map.Entry<DependencyType, Integer> entry : node.getNodeDependencyFrom(fromNode).entrySet()) {
					weight += entry.getKey().getBackwardWeight() * entry.getValue();
				}
				if (weight > 0.0f) {
					float distance = node.getImpact() + 1.0f / weight;
					if (distance < fromNode.getImpact()) {
						fromNode.setImpact(distance);
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
