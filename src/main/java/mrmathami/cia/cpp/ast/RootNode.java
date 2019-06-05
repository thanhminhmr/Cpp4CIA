package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class RootNode extends Node implements IRoot {
	private static final long serialVersionUID = 3605965717499471633L;

	@Nonnull
	private final List<INode> integrals;

	private RootNode(@Nonnull List<INode> integrals) {
		super("ROOT", "ROOT", "ROOT");
		this.integrals = integrals;
	}

	@Nonnull
	public static IRootBuilder builder() {
		return new RootNodeBuilder();
	}

	@Nonnull
	@Override
	public final List<INode> getIntegrals() {
		return Collections.unmodifiableList(integrals);
	}

	@Override
	public final boolean addIntegrals(@Nonnull List<INode> integrals) {
		if (integrals.isEmpty()) return true;
		if (!super.addChildren(integrals)) return false;

		return this.integrals.addAll(integrals);
	}

	@Override
	public final List<INode> removeIntegrals() {
		if (integrals.isEmpty()) return List.of();

		final List<INode> oldIntegrals = List.copyOf(integrals);
		integrals.clear();
		return oldIntegrals;
	}

	@Override
	public final boolean addIntegral(@Nonnull INode integral) {
		//noinspection ConstantConditions
		return super.addChild(integral) && integrals.add(integral);
	}

	@Override
	public final boolean removeIntegral(@Nonnull INode integral) {
		return super.removeChild(integral) && integrals.remove(integral);
	}

	@Override
	public final boolean replaceIntegral(@Nonnull INode oldIntegral, @Nonnull INode newIntegral) {
		if (!super.replaceChild(oldIntegral, newIntegral)) return false;

		final int index = integrals.indexOf(oldIntegral);
		if (index < 0) return false;
		if (integrals.contains(newIntegral)) {
			integrals.remove(index);
		} else {
			integrals.set(index, newIntegral);
		}
		return true;
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
		@Nonnull
		private List<INode> integrals = new ArrayList<>();

		private RootNodeBuilder() {
			setName("ROOT");
			setUniqueName("ROOT");
			setSignature("ROOT");
		}

		@Nonnull
		@Override
		public final IRoot build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			return new RootNode(integrals);
		}

		@Nonnull
		public final List<INode> getIntegrals() {
			return integrals;
		}

		@Nonnull
		public final IRootBuilder setIntegrals(@Nonnull List<INode> integrals) {
			this.integrals = new ArrayList<>(integrals);
			return this;
		}
	}
}
