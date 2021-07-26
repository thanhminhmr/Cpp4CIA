package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

class ValuePreprocessingToken extends PreprocessingToken {
	@Nullable Object value;

	ValuePreprocessingToken(@Nonnull TokenType type, int line, int column, @Nonnull String text) {
		super(type, line, column, text);
	}

	ValuePreprocessingToken(@Nonnull TokenType type, int line, int column, @Nonnull String text, @Nullable Object value) {
		super(type, line, column, text);
		this.value = value;
	}
}
