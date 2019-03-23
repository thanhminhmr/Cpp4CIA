package cia.cpp.ast;

import javax.annotation.Nonnull;

public final class FieldNode extends MemberTypeContainer implements IField {
	public FieldNode(@Nonnull String name, @Nonnull Visibility visibility) {
		super(name, visibility);
	}
}
