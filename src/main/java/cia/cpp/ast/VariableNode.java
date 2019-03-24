package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class VariableNode extends Node implements IVariable {
	@Nonnull
	private final Visibility visibility;
	@Nullable
	private final IType type;

	public VariableNode(@Nonnull String name, @Nonnull Visibility visibility, @Nullable IType type) {
		super(name);
		this.visibility = visibility;
		this.type = type;
	}

	public static VariableNodeBuilder builder() {
		return new VariableNodeBuilder();
	}

	@Nonnull
	@Override
	public final Visibility getVisibility() {
		return visibility;
	}

	@Nullable
	@Override
	public final IType getType() {
		return type;
	}

	public static final class VariableNodeBuilder extends NodeBuilder<VariableNode, VariableNodeBuilder> {
		@Nullable
		private Visibility visibility;
		@Nullable
		private IType type;

		private VariableNodeBuilder() {
		}

		@Nonnull
		@Override
		public final VariableNode build() {
			if (name == null || visibility == null) {
				throw new NullPointerException("Builder element(s) is null.");
			}
			return new VariableNode(name, visibility, type);
		}

		@Nullable
		public final Visibility getVisibility() {
			return visibility;
		}

		@Nonnull
		public final VariableNodeBuilder setVisibility(@Nullable Visibility visibility) {
			this.visibility = visibility;
			return this;
		}

		@Nullable
		public final IType getType() {
			return type;
		}

		@Nonnull
		public final VariableNodeBuilder setType(@Nullable IType type) {
			this.type = type;
			return this;
		}
	}
}
