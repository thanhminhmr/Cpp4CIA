package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;

import java.io.IOException;

public final class PreprocessorException extends IOException {
	private static final long serialVersionUID = -1L;

	public PreprocessorException(@Nonnull String message) {
		super(message);
	}

	public PreprocessorException(@Nonnull String message, @Nonnull Throwable cause) {
		super(message, cause);
	}
}
