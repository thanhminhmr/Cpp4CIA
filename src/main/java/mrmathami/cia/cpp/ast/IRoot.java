package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.Set;

@IAstComponent
public interface IRoot extends IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer, IIntegralContainer {

	void calculateImpact(@Nonnull Set<INode> changeSet);

	/**
	 * The root builder
	 */
	interface IRootBuilder extends INodeBuilder<IRoot, IRootBuilder> {
	}
}
