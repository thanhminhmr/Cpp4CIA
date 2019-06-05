package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class EnumNode extends Node implements IEnum {
	private static final long serialVersionUID = -8311215223911382978L;

	private EnumNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName) {
		super(name, simpleName, uniqueName);
	}

	@Nonnull
	public static IEnumBuilder builder() {
		return new EnumNodeBuilder();
	}

	@Nonnull
	@Override
	public final List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}

	public static final class EnumNodeBuilder extends NodeBuilder<IEnum, IEnumBuilder> implements IEnumBuilder {
		private EnumNodeBuilder() {
		}

		@Nonnull
		@Override
		public final IEnum build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new EnumNode(name, uniqueName, signature);
		}
	}
}
