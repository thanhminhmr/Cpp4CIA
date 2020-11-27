package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;
import java.util.List;

public interface IClassContainer {
	@Nonnull
	List<ClassNode> getClasses();
}
