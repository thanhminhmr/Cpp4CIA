package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface ITypedefContainer extends INode {
	@Nonnull
	List<ITypedef> getTypedefs();
}
