package cia.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IFunctionContainer extends INode {
	@Nonnull
	List<IFunction> getFunctions();
}
