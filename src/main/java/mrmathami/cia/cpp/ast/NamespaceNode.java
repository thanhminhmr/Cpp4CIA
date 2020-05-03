package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public final class NamespaceNode extends Node implements IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer {
	private static final long serialVersionUID = 4485573387809940424L;

	public NamespaceNode() {
	}

	public NamespaceNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		super(name, uniqueName, signature);
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

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
	}
	//</editor-fold>
}
