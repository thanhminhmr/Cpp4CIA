package cia.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class FunctionNode extends TypeContainer implements IFunction {
	public FunctionNode(@Nonnull String name) {
		super(name);
	}

	@Nonnull
	@Override
	public final List<IParameter> getParameters() {
		return getChildrenList(IParameter.class);
	}

	@Nonnull
	@Override
	public List<IClass> getComposites() {
		return getChildrenList(IClass.class);
	}
}
