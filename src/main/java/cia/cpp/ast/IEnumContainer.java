package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstFragment
public interface IEnumContainer extends INode {
	@Nonnull
	List<INode> getEnums();
}
