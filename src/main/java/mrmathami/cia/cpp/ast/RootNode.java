package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.List;

public final class RootNode extends CppNode implements IIntegralContainer, IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = -1L;

	private int nodeCount;

	public RootNode() {
		setName("ROOT");
		setUniqueName("ROOT");
		setSignature("ROOT");
	}

	public void lock() {
		final HashMap<String, String> stringPool = new HashMap<>();
		final HashMap<DependencyMap, DependencyMap> countsPool = new HashMap<>();
		if (isWritable()) internalLock(stringPool, countsPool);
		for (final CppNode node : this) {
			if (node.isWritable()) node.internalLock(stringPool, countsPool);
		}
		stringPool.clear();
		countsPool.clear();
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		checkReadOnly();
		this.nodeCount = nodeCount;
	}

	//region Containers

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

	//endregion Containers

	@Nonnull
	@Override
	String partialElementString() {
		return ", \"nodeCount\": " + nodeCount;
	}

	//region Object Helper

	@Override
	public void writeExternal(@Nonnull ObjectOutput output) throws IOException {
		super.writeExternal(output);
		lock();
	}

	//endregion Object Helper
}
