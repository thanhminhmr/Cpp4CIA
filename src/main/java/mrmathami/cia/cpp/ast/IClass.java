package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.Set;

@IAstComponent
public interface IClass extends IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer {
	@Nonnull
	Set<INode> getBases();

	void removeBases();

	boolean addBase(@Nonnull INode base);

	boolean removeBase(@Nonnull INode base);

	boolean replaceBase(@Nonnull INode oldBase, @Nonnull INode newBase);

	/**
	 * The class builder
	 */
	interface IClassBuilder extends INodeBuilder<IClass, IClassBuilder> {
		@Nonnull
		Set<INode> getBases();

		@Nonnull
		IClassBuilder setBases(@Nonnull Set<INode> bases);
	}
}
