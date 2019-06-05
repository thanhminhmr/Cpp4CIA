package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class VariableNode extends Node implements IVariable {
	private static final long serialVersionUID = 245317490612142346L;

	@Nullable
	private INode type;

	private VariableNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nullable INode type) {
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

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass() || !super.equals(object)) return false;
		final VariableNode that = (VariableNode) object;
		return Objects.equals(type, that.type);

	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	@Nonnull
	@Override
	public final String toString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + getName()
				+ "\", uniqueName: \"" + getUniqueName()
				+ "\", signature: \"" + getSignature()
				+ "\", directWeight: " + getDirectWeight()
//				+ ", indirectWeight: " + getIndirectWeight()
				+ ", type: " + type
				+ " }";
	}

	@Nonnull
	@Override
	public final String toTreeElementString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + getName()
				+ "\", uniqueName: \"" + getUniqueName()
				+ "\", signature: \"" + getSignature()
				+ "\", directWeight: " + getDirectWeight()
//				+ ", indirectWeight: " + getIndirectWeight()
				+ ", dependencyMap: " + Utilities.mapToString(getDependencies())
				+ ", type: " + type
				+ " }";
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
			return new VariableNode(name, uniqueName, signature, type);
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
