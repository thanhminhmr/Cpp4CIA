package mrmathami.cia.cpp.ast;

import javax.annotation.Nullable;

/**
 * Hold a type
 */
public interface ITypeContainer {
	@Nullable
	Node getType();

	boolean setType(@Nullable Node type);
}
