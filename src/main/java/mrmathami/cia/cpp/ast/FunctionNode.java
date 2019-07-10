package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class FunctionNode extends Node implements IFunction {
	private static final long serialVersionUID = -1625561533522078792L;

	@Nonnull
	private final List<INode> parameters;

	@Nullable
	private INode type;

	@Nullable
	private String body;

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
	public final List<INode> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	@Override
	public final void removeParameters() {
		for (final INode parameter : parameters) {
			removeChild(parameter);
		}
		parameters.clear();
	}

	@Override
	public final boolean addParameter(@Nonnull INode parameter) {
		//noinspection ConstantConditions
		return super.addChild(parameter) && parameters.add(parameter);
	}

	@Override
	public final boolean removeParameter(@Nonnull INode parameter) {
		return super.removeChild(parameter) && parameters.remove(parameter);
	}

	@Override
	public final boolean replaceParameter(@Nonnull INode oldParameter, @Nonnull INode newParameter) {
		final int index = parameters.indexOf(oldParameter);
		if (index < 0) return false;
		parameters.set(index, newParameter);
		return true;
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

	@Nullable
	@Override
	public final String getBody() {
		return body;
	}

	@Override
	public final void setBody(@Nullable String body) {
		this.body = body;
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
	public final List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass() || !super.equals(object)) return false;
		final FunctionNode node = (FunctionNode) object;
		return Objects.equals(type, node.type);
	}

	@Override
	public final int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (type != null ? type.hashCode() : 0);
		return result;
	}

	@Override
	public final boolean matches(Object node) {
		return super.matches(node) && Objects.equals(((IFunction) node).getBody(), body);
	}

	@Nonnull
	@Override
	protected String partialTreeElementString() {
		return ", type: " + type
				+ ", parameters: " + Utilities.collectionToString(parameters)
				+ ", body: " + (body != null ? "\"" + body.replaceAll("\"", "\"\"") + "\"" : null);
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
			return new FunctionNode(name, uniqueName, signature, parameters, type);
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
