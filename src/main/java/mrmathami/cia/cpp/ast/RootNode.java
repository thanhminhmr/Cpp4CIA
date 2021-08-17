package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;
import java.util.List;

public final class RootNode extends CppNode implements IIntegralContainer, IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = 4616684807799617419L;

	private int nodeCount;

	public RootNode() {
		super("ROOT", "ROOT", "ROOT");
	}

	public void lock() {
		if (isWritable()) internalLock();
	}

	public int getNodeCount() {
		return nodeCount;
	}

	@Nonnull
	public RootNode setNodeCount(int nodeCount) {
		checkReadOnly();
		this.nodeCount = nodeCount;
		return this;
	}

	@Nonnull
	@Override
	public List<IntegralNode> getIntegrals() {
		return getChildrenList(IntegralNode.class);
	}

	@Nonnull
	@Override
	public List<ClassNode> getClasses() {
		return getChildrenList(ClassNode.class);
	}

	@Nonnull
	@Override
	public List<EnumNode> getEnums() {
		return getChildrenList(EnumNode.class);
	}

	@Nonnull
	@Override
	public List<FunctionNode> getFunctions() {
		return getChildrenList(FunctionNode.class);
	}

	@Nonnull
	@Override
	public List<VariableNode> getVariables() {
		return getChildrenList(VariableNode.class);
	}

	@Nonnull
	@Override
	public List<TypedefNode> getTypedefs() {
		return getChildrenList(TypedefNode.class);
	}

	@Nonnull
	@Override
	String partialElementString() {
		return ", \"nodeCount\": " + nodeCount;
	}
}
