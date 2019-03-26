package cia.cpp.ast;

import javax.annotation.Nullable;

/**
 * Hold a type
 */
@IAstFragment
public interface ITypeContainer extends INode {
	@Nullable
	IType getType();

	void setType(@Nullable IType type);
}
