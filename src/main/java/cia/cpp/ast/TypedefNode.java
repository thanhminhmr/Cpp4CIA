package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class TypedefNode extends Node implements ITypedef {
	@Nullable
	private IType type;

	private TypedefNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nullable IType type) {
		super(name, simpleName, uniqueName);
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

	@Override
	public final void setType(@Nullable IType type) {
		this.type = type;
	}

	public static final class TypedefNodeBuilder extends NodeBuilder<TypedefNode, TypedefNodeBuilder> {
		@Nullable
		private IType type;

		private TypedefNodeBuilder() {
		}

		@Nonnull
		@Override
		public final TypedefNode build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new TypedefNode(name, simpleName, uniqueName, type);
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
