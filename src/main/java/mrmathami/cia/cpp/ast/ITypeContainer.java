package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Internal;
import mrmathami.annotations.Nullable;

/**
 * Hold a type
 */
public interface ITypeContainer {
	@Nullable
	CppNode getType();

	@Internal
	void setType(@Nullable CppNode type);
}
