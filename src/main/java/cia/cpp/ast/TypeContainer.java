package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class TypeContainer extends Node implements ITypeContainer {
	@Nullable
	private IType type;

	protected TypeContainer(@Nonnull String name) {
		super(name);
	}

	@Nullable
	@Override
	public final IType getType() {
		return type;
	}

	public final void setType(@Nullable IType type) {
		this.type = type;
	}
}
