package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class NamespaceNode extends Node implements IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer {
	private static final long serialVersionUID = -7182112379517811654L;

	public NamespaceNode() {
	}

	@Nonnull
	@Override
	public final List<ClassNode> getClasses() {
		return getChildrenList(ClassNode.class);
	}

	@Nonnull
	@Override
	public final List<EnumNode> getEnums() {
		return getChildrenList(EnumNode.class);
	}

	@Nonnull
	@Override
	public final List<FunctionNode> getFunctions() {
		return getChildrenList(FunctionNode.class);
	}

	@Nonnull
	@Override
	public final List<VariableNode> getVariables() {
		return getChildrenList(VariableNode.class);
	}
}
