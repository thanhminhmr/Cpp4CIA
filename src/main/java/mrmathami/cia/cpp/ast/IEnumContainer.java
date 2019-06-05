package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstFragment
public interface IEnumContainer extends INode {
	@Nonnull
	List<IEnum> getEnums();
}
