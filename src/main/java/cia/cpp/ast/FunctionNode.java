package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class FunctionNode extends Node implements IFunction {
	@Nonnull
	private final Visibility visibility;
	@Nullable
	private IType type;

	private FunctionNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nonnull Visibility visibility, @Nullable IType type) {
		super(name, simpleName, uniqueName);
		this.visibility = visibility;
		this.type = type;
	}

	public static FunctionNodeBuilder builder() {
		return new FunctionNodeBuilder();
	}

//	@Nonnull
//	@Override
//	public final List<IParameter> getParameters() {
//		return getChildrenList(IParameter.class);
//	}

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

	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ") { " + super.toString()
				+ "\", visibility = " + visibility
				+ ", type = " + type + " }";
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
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new FunctionNode(name, simpleName, uniqueName, visibility, type);
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
