package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstComponent
public interface IClass extends IClassContainer, IEnumContainer, ITypedefContainer, IFunctionContainer, IVariableContainer {
	@Nonnull
	List<INode> getBases();

	boolean addBase(@Nonnull INode base);

	boolean removeBase(@Nonnull INode base);

	boolean replaceBase(@Nonnull INode oldBase, @Nonnull INode newBase);

	/**
	 * The class builder
	 */
	interface IClassBuilder extends INodeBuilder<IClass, IClassBuilder> {
		@Nonnull
		List<INode> getBases();

		@Nonnull
		IClassBuilder setBases(@Nonnull List<INode> bases);
	}
}
