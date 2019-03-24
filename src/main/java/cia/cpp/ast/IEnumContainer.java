package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IEnumContainer extends INode {
	@Nonnull
	List<IEnum> getEnums();
}
