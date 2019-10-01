package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IBodyContainer<E extends Node> {
	@Nullable
	String getBody();

	@Nonnull
	E setBody(@Nullable String body);
}
