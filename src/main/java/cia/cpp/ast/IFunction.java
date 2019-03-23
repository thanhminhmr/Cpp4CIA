package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@AstComponent
public interface IFunction extends ICompositeContainer, ITypeContainer, IType {
	@Nonnull
	List<IParameter> getParameters();
}
