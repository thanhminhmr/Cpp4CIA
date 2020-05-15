package mrmathami.cia.cpp.ast;

import javax.annotation.Nullable;

/**
 * Hold a type
 */
public interface ITypeContainer {
	@Nullable
	CppNode getType();

	boolean setType(@Nullable CppNode type);
}
