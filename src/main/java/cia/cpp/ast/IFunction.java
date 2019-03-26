package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstComponent
public interface IFunction extends IMember, IType, ITypeContainer, IClassContainer, IEnumContainer, ITypedefContainer, IVariableContainer {
//	@Nonnull
//	List<IParameter> getParameters();
}
