package mrmathami.cia.cpp.preprocessor.token;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import mrmathami.cia.cpp.preprocessor.source.TokenSource;

import java.io.Serializable;

public final class Token implements Serializable {
	@Nonnull private final TokenType type;
	@Nonnull private final String text;
	@Nullable private final TokenSource source;
	private final int startLine;
	private final int startColumn;
	private final int endLine;
	private final int endColumn;

	public Token(@Nonnull TokenType type) {
		assert !type.isTextContainer();
		this.type = type;
		this.text = type.toString();
		this.source = null;
		this.startLine = 0;
		this.startColumn = 0;
		this.endLine = 0;
		this.endColumn = 0;
	}

	public Token(@Nonnull TokenType type, @Nonnull String text) {
		assert type.isTextContainer();
		this.type = type;
		this.text = text;
		this.source = null;
		this.startLine = 0;
		this.startColumn = 0;
		this.endLine = 0;
		this.endColumn = 0;
	}

	public Token(@Nonnull TokenType type, @Nonnull TokenSource source,
			int startLine, int startColumn, int endLine, int endColumn) {
		assert !type.isTextContainer();
		this.type = type;
		this.text = type.toString();
		this.source = source;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	public Token(@Nonnull TokenType type, @Nonnull String text, @Nonnull TokenSource source,
			int startLine, int startColumn, int endLine, int endColumn) {
		assert type.isTextContainer();
		this.type = type;
		this.text = text;
		this.source = source;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	@Nonnull
	public TokenType getType() {
		return type;
	}

	@Nonnull
	public String getText() {
		return text;
	}

	@Nullable
	public TokenSource getSource() {
		return source;
	}

	public int getStartLine() {
		return startLine;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public int getEndLine() {
		return endLine;
	}

	public int getEndColumn() {
		return endColumn;
	}

	@Override
	public String toString() {
		return String.format("[%d:%d@%d:%d] %s %s", startLine, startColumn, endLine, endColumn, type.name(), text);
	}
}
