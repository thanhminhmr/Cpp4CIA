package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class RootNode extends CppNode implements IIntegralContainer, IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = 4616684807799617419L;

	private int nodeCount;

	public RootNode() {
		super("ROOT", "ROOT", "ROOT");
	}

	public final void lock() {
		if (isWritable()) internalLock();
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
	public final List<TypedefNode> getTypedefs() {
		return getChildrenList(TypedefNode.class);
	}

	@Nonnull
	@Override
	final String partialTreeElementString() {
		return ", nodeCount: " + nodeCount;
	}
}
