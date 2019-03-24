package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IClassContainer extends INode {
	@Nonnull
	List<IClass> getClasses();
}
