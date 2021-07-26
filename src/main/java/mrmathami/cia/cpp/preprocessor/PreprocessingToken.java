package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.Serializable;

public class PreprocessingToken implements Serializable {
	@Nonnull public final TokenType type;
	public final int line;
	public final int column;
	@Nonnull public final String text;

	PreprocessingToken(@Nonnull TokenType type, int line, int column) {
		assert !type.isTextContainer();
		this.type = type;
		this.line = line;
		this.column = column;
		this.text = type.toString();
	}

	PreprocessingToken(@Nonnull TokenType type, int line, int column, @Nonnull String text) {
		assert type.isTextContainer();
		this.type = type;
		this.line = line;
		this.column = column;
		this.text = text;
	}
}
