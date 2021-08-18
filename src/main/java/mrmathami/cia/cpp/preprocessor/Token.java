package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;
import mrmathami.cia.cpp.preprocessor.TokenType;

import java.io.Serializable;

public final class Token implements Serializable {
	@Nonnull private final TokenType type;
	private final int line;
	private final int column;
	@Nonnull private final String text;

	Token(@Nonnull TokenType type, int line, int column) {
		assert !type.isTextContainer();
		this.type = type;
		this.line = line;
		this.column = column;
		this.text = type.toString();
	}

	Token(@Nonnull TokenType type, int line, int column, @Nonnull String text) {
		assert type.isTextContainer();
		this.type = type;
		this.line = line;
		this.column = column;
		this.text = text;
	}

	@Nonnull
	public TokenType getType() {
		return type;
	}

	public int getLine() {
		return line;
	}

	public int getColumn() {
		return column;
	}

	@Nonnull
	public String getText() {
		return text;
	}
}
