package cia.cpp.ast;

import javax.annotation.Nonnull;

@IAstFragment
public interface IMember extends IType {
	@Nonnull
	Visibility getVisibility();

	enum Visibility {
		PUBLIC,
		PROTECTED,
		PRIVATE
	}
}
