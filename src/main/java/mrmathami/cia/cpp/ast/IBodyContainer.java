package mrmathami.cia.cpp.ast;

import javax.annotation.Nullable;

public interface IBodyContainer {
	@Nullable
	String getBody();

	void setBody(@Nullable String body);
}
