package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstFragment
public interface IClassContainer extends INode {
	@Nonnull
	List<IClass> getClasses();
}
