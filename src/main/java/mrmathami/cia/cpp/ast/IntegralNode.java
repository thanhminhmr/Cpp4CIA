package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;

public final class IntegralNode extends CppNode {
	private static final long serialVersionUID = -1L;

	public IntegralNode() {
	}

	public IntegralNode(@Nonnull String name) {
		setName(name);
		setUniqueName(name);
		setSignature(name);
	}
}
