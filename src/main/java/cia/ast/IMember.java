package cia.ast;

import javax.annotation.Nonnull;

public interface IMember extends IType {
	@Nonnull
	Visibility getVisibility();
}
