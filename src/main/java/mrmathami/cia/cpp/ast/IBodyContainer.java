package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Internal;
import mrmathami.annotations.Nullable;

public interface IBodyContainer {
	@Nullable
	String getBody();

	@Internal
	void setBody(@Nullable String body);
}
