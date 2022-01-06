package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;

import java.io.IOException;
import java.io.ObjectOutput;
import java.util.List;

public final class NamespaceNode extends CppNode implements IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = -1L;

	public NamespaceNode() {
	}

	//region Containers

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

	//region Object Helper

	@Override
	public void writeExternal(@Nonnull ObjectOutput output) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		super.writeExternal(output);
	}

	//endregion Object Helper
}
