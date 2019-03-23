package cia.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class EnumNode extends TypeContainer implements IEnum {
	public EnumNode(@Nonnull String name) {
		super(name);
	}

	@Nonnull
	@Override
	public List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}
}
