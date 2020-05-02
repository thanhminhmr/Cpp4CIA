package mrmathami.cia.cpp;

import javax.annotation.Nonnull;

public final class CppException extends Exception {
	private static final long serialVersionUID = 1846183639625886856L;

	public CppException(@Nonnull String message) {
		super(message);
	}

	public CppException(@Nonnull String message, @Nonnull Throwable cause) {
		super(message, cause);
	}
}
