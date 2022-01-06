package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;

import java.io.IOException;
import java.io.ObjectOutput;

public final class IntegralNode extends CppNode {
	private static final long serialVersionUID = -1L;

	public IntegralNode() {
	}

	public IntegralNode(@Nonnull String name) {
		setName(name);
		setUniqueName(name);
		setSignature(name);
	}

	//region Object Helper

	@Override
	public void writeExternal(@Nonnull ObjectOutput output) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		super.writeExternal(output);
	}

	//endregion Object Helper
}
