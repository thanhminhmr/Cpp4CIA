package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IClass extends IMember, IType, IClassContainer, IEnumContainer, ITypedefContainer, IFunctionContainer, IVariableContainer {
	@Nonnull
	List<IClass> getBases();

	@Nonnull
	Visibility getChildDefaultVisibility();
}
