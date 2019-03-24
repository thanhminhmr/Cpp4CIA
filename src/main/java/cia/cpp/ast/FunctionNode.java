package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class FunctionNode extends Node implements IFunction {
	@Nonnull
	private final Visibility visibility;
	@Nullable
	private final IType type;

	private FunctionNode(@Nonnull String name, @Nonnull Visibility visibility, @Nullable IType type) {
		super(name);
		this.visibility = visibility;
		this.type = type;
	}

	public static FunctionNodeBuilder builder() {
		return new FunctionNodeBuilder();
	}

	@Nonnull
	@Override
	public final List<IParameter> getParameters() {
		return getChildrenList(IParameter.class);
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
	public final List<ITypedef> getTypedefs() {
		return getChildrenList(ITypedef.class);
	}

	@Nonnull
	@Override
	public final List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}

	public static final class FunctionNodeBuilder extends NodeBuilder<FunctionNode, FunctionNodeBuilder> {
		@Nullable
		private Visibility visibility;
		@Nullable
		private IType type;

		private FunctionNodeBuilder() {
		}

		@Nonnull
		@Override
		public final FunctionNode build() {
			if (name == null || visibility == null) {
				throw new NullPointerException("Builder element(s) is null.");
			}
			return new FunctionNode(name, visibility, type);
		}

		@Nullable
		public final Visibility getVisibility() {
			return visibility;
		}

		@Nonnull
		public final FunctionNodeBuilder setVisibility(@Nullable Visibility visibility) {
			this.visibility = visibility;
			return this;
		}

		@Nullable
		public final IType getType() {
			return type;
		}

		@Nonnull
		public final FunctionNodeBuilder setType(@Nullable IType type) {
			this.type = type;
			return this;
		}
	}
}
