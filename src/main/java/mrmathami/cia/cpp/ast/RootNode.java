package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class RootNode extends Node implements IIntegralContainer, IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer {
	private static final long serialVersionUID = -6990993399934049219L;

	public RootNode() {
		setName("ROOT");
		setUniqueName("ROOT");
		setSignature("ROOT");
	}

	@Nonnull
	@Override
	public final List<IntegralNode> getIntegrals() {
		return getChildrenList(IntegralNode.class);
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
