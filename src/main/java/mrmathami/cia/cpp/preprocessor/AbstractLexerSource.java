package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;

import java.io.IOException;

abstract class AbstractLexerSource implements TokenSource {
	@Nonnull private final LexerReader reader;
	@Nonnull private final EventListener listener;

	AbstractLexerSource(@Nonnull LexerReader reader, @Nonnull EventListener listener) {
		this.reader = reader;
		this.listener = listener;
	}

	// ====

	private void postWarning(@Nonnull String message) throws PreprocessorException {
		listener.handleWarning(this, reader.getLine(), reader.getColumn(), message);
	}

	private void postError(@Nonnull String message) throws PreprocessorException {
		listener.handleError(this, reader.getLine(), reader.getColumn(), message);
	}

	private void postCritical(@Nonnull String message) throws PreprocessorException {
		final int line = reader.getLine();
		final int column = reader.getColumn();
		listener.handleCritical(this, line, column, message);
		throw new PreprocessorException("Critical error at " + getName() + '[' + line + ':' + column + "]: " + message);
	}

	// ====

	private static boolean isIdentifierUCN(int cp) {
		return cp == 0xA8 || cp == 0xAA || cp == 0xAD || cp == 0xAF
				|| cp >= 0xB2 && cp <= 0x1FFF
				&& cp != 0xB6 && cp != 0xBB && cp != 0xBF && cp != 0xD7 && cp != 0xF7
				&& cp != 0x1680 && cp != 0x180E
				|| cp >= 0x200B && cp <= 0x200D || cp >= 0x202A && cp <= 0x202E
				|| cp == 0x203F || cp == 0x2040 || cp == 0x2054
				|| cp >= 0x2060 && cp <= 0x218F || cp >= 0x2460 && cp <= 0x24FF
				|| cp >= 0x2776 && cp <= 0x2793 || cp >= 0x2C00 && cp <= 0x2DFF
				|| cp >= 0x2E80 && cp <= 0x2FFF || cp >= 0x3004 && cp <= 0x3007
				|| cp >= 0x3021 && cp <= 0xD7FF && cp != 0x3030
				|| cp >= 0xF900 && cp <= 0xFD3D || cp >= 0xFD40 && cp <= 0xFDCF
				|| cp >= 0xFDF0 && cp <= 0xFE44 || cp >= 0xFE47 && cp <= 0xFFFD
				|| cp >= 0x10000 && (cp & 0xFFFF) <= 0xFFFD;
	}

	private static boolean isIdentifierInitialUCN(int cp) {
		return cp == 0xA8 || cp == 0xAA || cp == 0xAD || cp == 0xAF
				|| cp >= 0xB2 && cp <= 0x02FF
				&& cp != 0xB6 && cp != 0xBB && cp != 0xBF && cp != 0xD7 && cp != 0xF7
				|| cp >= 0x0370 && cp <= 0x1DBF && cp != 0x1680 && cp != 0x180E
				|| cp >= 0x1E00 && cp <= 0x1FFF || cp >= 0x200B && cp <= 0x200D
				|| cp >= 0x202A && cp <= 0x202E || cp == 0x203F || cp == 0x2040 || cp == 0x2054
				|| cp >= 0x2060 && cp <= 0x20CF || cp >= 0x2100 && cp <= 0x218F
				|| cp >= 0x2460 && cp <= 0x24FF || cp >= 0x2776 && cp <= 0x2793
				|| cp >= 0x2C00 && cp <= 0x2DFF || cp >= 0x2E80 && cp <= 0x2FFF
				|| cp >= 0x3004 && cp <= 0x3007 || cp >= 0x3021 && cp <= 0xD7FF && cp != 0x3030
				|| cp >= 0xF900 && cp <= 0xFD3D || cp >= 0xFD40 && cp <= 0xFDCF
				|| cp >= 0xFDF0 && cp <= 0xFE1F || cp >= 0xFE30 && cp <= 0xFE44
				|| cp >= 0xFE47 && cp <= 0xFFFD || cp >= 0x10000 && (cp & 0xFFFF) <= 0xFFFD;
	}

	private static boolean isDigit(int cp) {
		return cp >= '0' && cp <= '9';
	}

	private static boolean isNonDigit(int cp) {
		return cp >= 'A' && cp <= 'Z' || cp == '_' || cp >= 'a' && cp <= 'z';
	}

	// ====

	@Nonnull
	private PreprocessingToken parseHeaderName(boolean isQuote) throws IOException, PreprocessorException {
		// Note: the open quote/bracket are already consumed
		final StringBuilder text = new StringBuilder(isQuote ? "\"" : "<");
		final int close = isQuote ? '\"' : '>';
		while (true) {
			final int c = reader.read();
			if (c == -1 || c == '\n') {
				postError("Unterminated header string literal");
				break;
			}
			text.appendCodePoint(c);
			if (c == close) break;
		}
		return createToken(TokenType.HEADER_NAME, text.toString());
	}

	@Nonnull
	private PreprocessingToken parseNumber(@Nonnull String open) throws IOException, PreprocessorException {
		// Note: the open digit (or dot digit) are already consumed
		final StringBuilder text = new StringBuilder(open);
		while (true) {
			final int c = reader.read();
			if (c == '.' || isDigit(c) || isNonDigit(c) || isIdentifierUCN(c)) {
				text.appendCodePoint(c);
				if (c == 'e' || c == 'E' || c == 'p' || c == 'P') {
					final int d = reader.read();
					if (d == '+' || d == '-') {
						text.appendCodePoint(d);
					} else {
						reader.unread();
					}
				}
			} else {
				if (c >= 0) reader.unread();
				return createToken(TokenType.NUMBER, text.toString());
			}
		}
	}

	@Nonnull
	private PreprocessingToken parseIdentifier(int openCP) throws IOException, PreprocessorException {
		// open codepoint is already checked
		final StringBuilder text = new StringBuilder().appendCodePoint(openCP);
		while (true) {
			final int c = reader.read();
			if (isDigit(c) || isNonDigit(c) || isIdentifierUCN(c)) {
				text.appendCodePoint(c);
			} else {
				if (c >= 0) reader.unread();
				return createToken(TokenType.IDENTIFIER, text.toString());
			}
		}
	}

	@Nonnull
	private PreprocessingToken parseBlockComment() throws IOException, PreprocessorException {
		// Note: the forward slash '/' and the star '*' are already consumed
		final StringBuilder text = new StringBuilder("/*");
		boolean matchingStar = false;
		while (true) {
			final int c = reader.read();
			if (c == -1) {
				postError("Unterminated comment");
				break;
			}
			text.appendCodePoint(c);
			if (matchingStar && c == '/') break;
			if ((c != '*') == matchingStar) matchingStar ^= true;
		}
		return createToken(TokenType.BLOCK_COMMENT, text.toString());
	}

	@Nonnull
	private PreprocessingToken parseLineComment() throws IOException, PreprocessorException {
		// Note: both the forward slash "//" are already consumed
		final StringBuilder text = new StringBuilder("//");
		while (true) {
			final int c = reader.read();
			if (c == -1 || c == '\n') break;
			text.appendCodePoint(c);
		}
		return createToken(TokenType.LINE_COMMENT, text.toString());
	}

	@Nonnull
	private PreprocessingToken parseCharacter(@Nonnull String open) throws IOException, PreprocessorException {
		// Note: the open quote are already consumed
		final StringBuilder text = new StringBuilder(open);
		boolean escape = false;
		while (true) {
			final int c = reader.read();
			if (c == -1 || c == '\n') {
				postError("Unterminated character literal");
				break;
			}
			text.appendCodePoint(c);
			if (escape) continue;
			if (c == '\\') {
				escape = true;
			} else if (c == '\'') {
				break;
			}
		}
		return createToken(TokenType.CHARACTER, text.toString());
	}

	@Nonnull
	private PreprocessingToken parseString(@Nonnull String open) throws IOException, PreprocessorException {
		// Note: the open quote are already consumed
		final StringBuilder text = new StringBuilder(open);
		boolean escape = false;
		while (true) {
			final int c = reader.read();
			if (c == -1 || c == '\n') {
				postError("Unterminated string literal");
				break;
			}
			text.appendCodePoint(c);
			if (escape) continue;
			if (c == '\\') {
				escape = true;
			} else if (c == '"') {
				break;
			}
		}
		return createToken(TokenType.STRING, text.toString());
	}

	@Nonnull
	private PreprocessingToken parseRawString(@Nonnull String open) throws IOException, PreprocessorException {
		// Note: the open quote are already consumed, but not the delimiter part
		final StringBuilder text = new StringBuilder(open);
		// parse delimiter now...
		final StringBuilder delimiter = new StringBuilder();
		{
			int count = 0;
			while (true) {
				final int c = reader.read();
				if (c == -1) {
					postError("Unterminated raw string literal");
					return createToken(TokenType.STRING, text.toString());
				}
				text.appendCodePoint(c);
				// end of delimiter
				if (c == '(') break;
				// invalid character in delimiter
				if (c <= ' ' || c >= '\u007F' || c == '$' || c == '@' || c == ')' || c == '\\' || c == '`') {
					postError("Invalid character in raw string separator");
					// try to find a quote to close raw string
					while (true) {
						final int d = reader.read();
						if (d == -1) break;
						text.appendCodePoint(d);
						if (d == '"') break;
					}
					return createToken(TokenType.STRING, text.toString());
				}
				delimiter.appendCodePoint(c);
				count += 1;
			}
			if (count > 16) postError("Raw string separator is longer than 16 characters");
		}
		{
			final int[] delimiterCPs = delimiter.codePoints().toArray();
			int count = -1;
			while (true) {
				final int c = reader.read();
				if (c == -1) {
					postError("Unterminated raw string literal");
					return createToken(TokenType.STRING, text.toString());
				}
				text.appendCodePoint(c);
				if (c == ')') {
					count = 0;
				} else if (c == '"' && count == delimiterCPs.length) {
					return createToken(TokenType.STRING, text.toString());
				} else if (count >= 0 && c != delimiterCPs[count++]) {
					count = -1;
				}
			}
		}
	}

	@Nonnull
	private PreprocessingToken parseWhitespace(@Nonnull int openCP) {

	}

	// ====

	private int line;
	private int column;

	private int readAndSaveLineColumn() throws IOException, PreprocessorException {
		final int cp = reader.read();
		this.line = reader.getLine();
		this.column = reader.getLine();
		return cp;
	}

	@Nonnull
	private PreprocessingToken createToken(@Nonnull TokenType type) {
		return new PreprocessingToken(type, line, column);
	}

	@Nonnull
	private PreprocessingToken createToken(@Nonnull TokenType type, @Nonnull String text) {
		return new PreprocessingToken(type, line, column, text);
	}

	// ====

	@Nonnull
	@Override
	public PreprocessingToken nextToken() throws IOException, PreprocessorException {
		final int c = readAndSaveLineColumn();
		switch ()
		return null;
	}


}
