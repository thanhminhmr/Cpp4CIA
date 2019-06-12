package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class NamespaceNode extends Node implements INamespace {
	private static final long serialVersionUID = 5599826257791515614L;

	private NamespaceNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName) {
		super(name, simpleName, uniqueName);
	}

	@Nonnull
	public static INamespaceBuilder builder() {
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
	public final List<IFunction> getFunctions() {
		return getChildrenList(IFunction.class);
	}

	@Nonnull
	@Override
	public final List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}

	public static final class NamespaceNodeBuilder extends NodeBuilder<INamespace, INamespaceBuilder> implements INamespaceBuilder {
		private NamespaceNodeBuilder() {
		}

		@Nonnull
		@Override
		public final INamespace build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new NamespaceNode(name, uniqueName, signature);
		}
	}
}
