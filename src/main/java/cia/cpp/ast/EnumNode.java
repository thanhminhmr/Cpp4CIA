package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class EnumNode extends Node implements IEnum {
	@Nonnull
	private final Visibility visibility;
	@Nullable
	private IType type;

	private EnumNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nonnull Visibility visibility, @Nullable IType type) {
		super(name, simpleName, uniqueName);
		this.visibility = visibility;
		this.type = type;
	}

	public static EnumNodeBuilder builder() {
		return new EnumNodeBuilder();
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

	@Override
	public final void setType(@Nullable IType type) {
		this.type = type;
	}

	@Nonnull
	@Override
	public final List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}

	public static final class EnumNodeBuilder extends NodeBuilder<EnumNode, EnumNodeBuilder> {
		@Nullable
		private Visibility visibility;
		@Nullable
		private IType type;

		private EnumNodeBuilder() {
		}

		@Nonnull
		@Override
		public final EnumNode build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new EnumNode(name, simpleName, uniqueName, visibility, type);
		}

		@Override
		boolean isValid() {
			return super.isValid() && visibility != null;
		}

		@Nullable
		public final Visibility getVisibility() {
			return visibility;
		}

		@Nonnull
		public final EnumNodeBuilder setVisibility(@Nullable Visibility visibility) {
			this.visibility = visibility;
			return this;
		}

		@Nullable
		public final IType getType() {
			return type;
		}

		@Nonnull
		public final EnumNodeBuilder setType(@Nullable IType type) {
			this.type = type;
			return this;
		}
	}
}

