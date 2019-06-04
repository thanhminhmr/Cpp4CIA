package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstFragment
public interface IFunctionContainer extends INode {
	@Nonnull
	List<IFunction> getFunctions();
}
