package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IClass extends ICompositeContainer, IMethodContainer, IFieldContainer, IType {
	@Nonnull
	List<IClass> getBases();

	@Nonnull
	Visibility getDefaultVisibility();
}
