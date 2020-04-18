package mrmathami.cia.cpp;

import javax.annotation.Nonnull;

public final class CppException extends Exception {
	public CppException(@Nonnull String message) {
		super(message);
	}

	public CppException(@Nonnull String message, @Nonnull Throwable cause) {
		super(message, cause);
	}
}
