package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IClass extends IClassContainer, IEnumContainer, ITypedefContainer, IFunctionContainer, IVariableContainer {
	@Nonnull
	List<IClass> getBases();

	boolean addBase(@Nonnull IClass base);

	boolean removeBase(@Nonnull IClass base);

	/**
	 * The class builder
	 */
	interface IClassBuilder extends INodeBuilder<IClass, IClassBuilder> {
		@Nonnull
		List<IClass> getBases();

		@Nonnull
		IClassBuilder setBases(@Nonnull List<IClass> bases);
	}
}
