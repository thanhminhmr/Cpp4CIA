package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface ICompositeContainer extends INode {
	@Nonnull
	List<IClass> getComposites();
}
