package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

abstract class ClassNode extends Node implements IClass {
	@Nonnull
	private final Visibility defaultVisibility;

	@Nonnull
	private List<IClass> bases = List.of();

	protected ClassNode(@Nonnull String name, @Nonnull Visibility defaultVisibility) {
		super(name);
		this.defaultVisibility = defaultVisibility;
	}

	@Override
	@Nonnull
	public final Visibility getDefaultVisibility() {
		return defaultVisibility;
	}

	@Nonnull
	@Override
	public final List<IClass> getBases() {
		return bases;
	}

	public final void setBases(@Nonnull List<IClass> bases) {
		this.bases = bases;
	}

	@Nonnull
	@Override
	public final List<IClass> getComposites() {
		return getChildrenList(IClass.class);
	}

	@Nonnull
	@Override
	public final List<IMethod> getMethods() {
		return getChildrenList(IMethod.class);
	}

	@Nonnull
	@Override
	public final List<IField> getFields() {
		return getChildrenList(IField.class);
	}
}
