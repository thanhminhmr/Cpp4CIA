package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.List;
import java.util.stream.StreamSupport;

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
		final CppNode[] nodes = StreamSupport.stream(spliterator(), false).toArray(CppNode[]::new);
		output.writeObject(nodes);

		write(output);
		for (final CppNode node : nodes) node.write(output);
	}

	@Override
	public void readExternal(@Nonnull ObjectInput input) throws IOException, ClassNotFoundException {
		final CppNode[] nodes = castNonnull(input.readObject(), CppNode[].class);

		read(input);
		for (final CppNode node : nodes) node.read(input);
	}

	//endregion Object Helper
}
