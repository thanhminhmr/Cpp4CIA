package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstComponent
public interface IRoot extends IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer {
	@Nonnull
	List<INode> getIntegrals();

	boolean addIntegrals(@Nonnull List<INode> integrals);

	List<INode> removeIntegrals();

	boolean addIntegral(@Nonnull INode integral);

	boolean removeIntegral(@Nonnull INode integral);

	boolean replaceIntegral(@Nonnull INode oldIntegral, @Nonnull INode newIntegral);

	/**
	 * The root builder
	 */
	interface IRootBuilder extends INodeBuilder<IRoot, IRootBuilder> {
		@Nonnull
		List<INode> getIntegrals();

		@Nonnull
		IRootBuilder setIntegrals(@Nonnull List<INode> integrals);
	}
}
