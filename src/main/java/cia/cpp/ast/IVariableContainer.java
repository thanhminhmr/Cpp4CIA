package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstFragment
public interface IVariableContainer extends INode {
	@Nonnull
	List<IVariable> getVariables();
}
