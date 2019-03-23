package cia.ast;

import javax.annotation.Nonnull;

abstract class MemberTypeContainer extends TypeContainer implements IMember {
	@Nonnull
	private Visibility visibility;

	protected MemberTypeContainer(@Nonnull String name, @Nonnull Visibility visibility) {
		super(name);
		this.visibility = visibility;
	}

	@Nonnull
	@Override
	public Visibility getVisibility() {
		return visibility;
	}

	public final void setVisibility(@Nonnull Visibility visibility) {
		this.visibility = visibility;
	}
}
