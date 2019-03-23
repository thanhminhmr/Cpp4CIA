package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IVariableContainer extends INode {
	@Nonnull
	List<IVariable> getVariables();
}
