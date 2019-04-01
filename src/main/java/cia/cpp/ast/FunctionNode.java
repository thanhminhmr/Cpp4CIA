package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class FunctionNode extends Node implements IFunction, Serializable {
	@Nonnull
	private final List<INode> parameters;

	@Nullable
	private INode type;

	private FunctionNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nonnull List<INode> parameters, @Nullable INode type) {
		super(name, simpleName, uniqueName);
		this.parameters = parameters;
		this.type = type;
	}

	@Nonnull
	public static IFunctionBuilder builder() {
		return new FunctionNodeBuilder();
	}

	@Nonnull
	@Override
	public List<INode> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	@Override
	public boolean addParameter(@Nonnull INode parameter) {
		return parameters.add(parameter);
	}

	@Override
	public boolean removeParameter(@Nonnull INode parameter) {
		return parameters.remove(parameter);
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

	@Nonnull
	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ") { " + super.toString()
				+ ", type = " + type + " }";
	}

	public static final class FunctionNodeBuilder extends NodeBuilder<IFunction, IFunctionBuilder> implements IFunctionBuilder {
		@Nonnull
		private List<INode> parameters = new ArrayList<>();

		@Nullable
		private INode type;

		private FunctionNodeBuilder() {
		}

		@Nonnull
		@Override
		public final IFunction build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new FunctionNode(name, uniqueName, content, parameters, type);
		}

		@Nonnull
		@Override
		public List<INode> getParameters() {
			return parameters;
		}

		@Nonnull
		@Override
		public IFunctionBuilder setParameters(@Nonnull List<INode> parameters) {
			this.parameters = new ArrayList<>(parameters);
			return this;
		}

		@Override
		@Nullable
		public final INode getType() {
			return type;
		}

		@Override
		@Nonnull
		public final IFunctionBuilder setType(@Nullable INode type) {
			this.type = type;
			return this;
		}
	}
}
