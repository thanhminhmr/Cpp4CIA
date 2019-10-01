package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IEnumContainer {
	@Nonnull
	List<EnumNode> getEnums();
}
