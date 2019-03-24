package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TypedefNode extends Node implements ITypedef {
	@Nullable
	private final IType type;

	private TypedefNode(@Nonnull String name, @Nullable IType type) {
		super(name);
		this.type = type;
	}

	public static TypedefNodeBuilder builder() {
		return new TypedefNodeBuilder();
	}

	@Nullable
	@Override
	public final IType getType() {
		return type;
	}

	public static final class TypedefNodeBuilder extends NodeBuilder<TypedefNode, TypedefNodeBuilder> {
		@Nullable
		private IType type;

		private TypedefNodeBuilder() {
		}

		@Nonnull
		@Override
		public final TypedefNode build() {
			if (name == null) {
				throw new NullPointerException("Builder element(s) is null.");
			}
			return new TypedefNode(name, type);
		}

		@Nullable
		public final IType getType() {
			return type;
		}

		@Nonnull
		public final TypedefNodeBuilder setType(@Nullable IType type) {
			this.type = type;
			return this;
		}
	}
}
