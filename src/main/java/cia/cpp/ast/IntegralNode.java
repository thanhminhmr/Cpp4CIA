package cia.cpp.ast;

import javax.annotation.Nonnull;

public final class IntegralNode extends Node implements IIntegral {
	private static final long serialVersionUID = 7672687082721187640L;

	private IntegralNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String content) {
		super(name, uniqueName, content);
	}

	@Nonnull
	public static IIntegralBuilder builder() {
		return new IntegralBuilder();
	}

	public static final class IntegralBuilder extends NodeBuilder<IIntegral, IIntegralBuilder> implements IIntegralBuilder {
		private IntegralBuilder() {
		}

		@Nonnull
		@Override
		public IIntegral build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new IntegralNode(name, uniqueName, signature);
		}
	}
}
