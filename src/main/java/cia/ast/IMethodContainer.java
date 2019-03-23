package cia.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IMethodContainer extends INode {
	@Nonnull
	List<IMethod> getMethods();
}
