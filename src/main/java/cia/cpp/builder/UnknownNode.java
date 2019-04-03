package cia.cpp.builder;

import cia.cpp.ast.Node;

import javax.annotation.Nonnull;

final class UnknownNode extends Node implements IUnknown {
	private UnknownNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String content) {
		super(name, uniqueName, content);
	}

	public static IUnknownBuilder builder() {
		return new UnknownBuilder();
	}

	private static final class UnknownBuilder extends NodeBuilder<IUnknown, IUnknownBuilder> implements IUnknownBuilder {
		private UnknownBuilder() {
		}

		@Nonnull
		@Override
		public IUnknown build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new UnknownNode(name, uniqueName, signature);
		}
	}
}
