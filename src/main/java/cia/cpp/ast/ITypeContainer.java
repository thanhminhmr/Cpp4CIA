package cia.cpp.ast;

import javax.annotation.Nullable;

/**
 * Hold a type
 */
@AstFragment
public interface ITypeContainer extends INode {
	@Nullable
	IType getType();
}
