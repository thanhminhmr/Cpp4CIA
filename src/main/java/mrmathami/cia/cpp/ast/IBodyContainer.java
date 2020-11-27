package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nullable;

public interface IBodyContainer {
	@Nullable
	String getBody();

	void setBody(@Nullable String body);
}
