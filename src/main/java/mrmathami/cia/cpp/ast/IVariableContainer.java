package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;
import java.util.List;

public interface IVariableContainer {
	@Nonnull
	List<VariableNode> getVariables();
}
