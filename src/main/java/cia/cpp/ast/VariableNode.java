package cia.cpp.ast;

import javax.annotation.Nonnull;

public final class VariableNode extends TypeContainer implements IVariable {
	public VariableNode(@Nonnull String name) {
		super(name);
	}
}
