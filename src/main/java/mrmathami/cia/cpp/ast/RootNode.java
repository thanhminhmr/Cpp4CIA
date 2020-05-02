package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class RootNode extends Node implements IIntegralContainer, IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer {
	private static final long serialVersionUID = -3810880760955772602L;

	private int nodeCount;

	public RootNode() {
		setName("ROOT");
		setUniqueName("ROOT");
		setSignature("ROOT");
	}

	public final void lock() {
		if (!isReadOnly()) internalLock();
	}

	public final int getNodeCount() {
		return nodeCount;
	}

	@Nonnull
	public final RootNode setNodeCount(int nodeCount) {
		checkReadOnly();
		this.nodeCount = nodeCount;
		return this;
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

	@Nonnull
	@Override
	final String partialTreeElementString() {
		return ", nodeCount: " + nodeCount;
	}
}
