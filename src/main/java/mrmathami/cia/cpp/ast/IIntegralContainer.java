package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstFragment
public interface IIntegralContainer extends INode {
	@Nonnull
	List<IIntegral> getIntegrals();
}
