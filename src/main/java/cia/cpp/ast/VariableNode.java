package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

public final class VariableNode extends Node implements IVariable, Serializable {
	@Nullable
	private INode type;

	public VariableNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nullable INode type) {
		super(name, simpleName, uniqueName);
		this.type = type;
	}

	@Nonnull
	public static IVariableBuilder builder() {
		return new VariableNodeBuilder();
	}

	@Nullable
	@Override
	public final INode getType() {
		return type;
	}

	@Override
	public final void setType(@Nullable INode type) {
		this.type = type;
	}

	@Nonnull
	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ") { " + super.toString()
				+ ", type = " + type + " }";
	}

	public static final class VariableNodeBuilder extends NodeBuilder<IVariable, IVariableBuilder> implements IVariableBuilder {
		@Nullable
		private INode type;

		private VariableNodeBuilder() {
		}

		@Nonnull
		@Override
		public final IVariable build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new VariableNode(name, uniqueName, content, type);
		}

		@Override
		@Nullable
		public final INode getType() {
			return type;
		}

		@Override
		@Nonnull
		public final IVariableBuilder setType(@Nullable INode type) {
			this.type = type;
			return this;
		}
	}
}
