package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public interface IFieldContainer extends INode {
	@Nonnull
	List<IField> getFields();
}
