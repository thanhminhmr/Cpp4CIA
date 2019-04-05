package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class RootNode extends Node implements IRoot {
	private static final long serialVersionUID = 8769009304807039468L;

	public RootNode() {
		super("ROOT", "ROOT", "ROOT");
	}

	@Nonnull
	@Override
	public final List<INode> getClasses() {
		return getChildrenList(IClass.class);
	}

	@Nonnull
	@Override
	public final List<INode> getEnums() {
		return getChildrenList(IEnum.class);
	}

	@Nonnull
	@Override
	public final List<INode> getFunctions() {
		return getChildrenList(IFunction.class);
	}

	@Nonnull
	@Override
	public final List<INode> getVariables() {
		return getChildrenList(IVariable.class);
	}
}
