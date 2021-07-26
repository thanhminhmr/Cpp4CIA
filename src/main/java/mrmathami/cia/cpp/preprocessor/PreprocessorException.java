package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;

public final class PreprocessorException extends Exception {
	private static final long serialVersionUID = -873047606310365953L;

	public PreprocessorException(@Nonnull String message) {
		super(message);
	}

	public PreprocessorException(@Nonnull String message, @Nonnull Throwable cause) {
		super(message, cause);
	}
}
