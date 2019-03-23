package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class MethodNode extends MemberTypeContainer implements IMethod {
	public MethodNode(@Nonnull String name, @Nonnull Visibility visibility) {
		super(name, visibility);
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
