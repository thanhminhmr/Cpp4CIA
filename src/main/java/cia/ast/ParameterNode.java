package cia.ast;

import javax.annotation.Nonnull;

public final class ParameterNode extends TypeContainer implements IParameter {
	public ParameterNode(@Nonnull String name) {
		super(name);
	}
}
