package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class NamespaceNode extends Node implements INamespace {
	private NamespaceNode(@Nonnull String name) {
		super(name);
	}

	public static NamespaceNodeBuilder builder() {
		return new NamespaceNodeBuilder();
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
	public final List<IFunction> getFunctions() {
		return getChildrenList(IFunction.class);
	}

	@Nonnull
	@Override
	public final List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}

	public static final class NamespaceNodeBuilder extends NodeBuilder<NamespaceNode, NamespaceNodeBuilder> {
		@Nonnull
		@Override
		public final NamespaceNode build() {
			if (name == null) {
				throw new NullPointerException("Builder element(s) is null.");
			}
			return new NamespaceNode(name);
		}
	}
}
