package cia.ast;

import javax.annotation.Nonnull;

public final class VariableNode extends TypeContainer implements IVariable {
	public VariableNode(@Nonnull String name) {
		super(name);
	}
}
