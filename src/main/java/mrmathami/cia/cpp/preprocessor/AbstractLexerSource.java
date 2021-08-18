package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.IOException;

abstract class AbstractLexerSource implements TokenSource {
	@Nonnull private final LexerReader reader;
	@Nonnull private final EventListener listener;
	private final boolean digraphs = true;
	private boolean isNewline = true;
	private boolean isInclude = false;

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

	private static boolean isIdentifierNonDigit(int cp) {
		return cp >= 'A' && cp <= 'Z' || cp == '_' || cp >= 'a' && cp <= 'z';
	}

	private static boolean isDigit(int cp) {
		return cp >= '0' && cp <= '9';
	}

	private static boolean isHexadecimalDigit(int cp) {
		return cp >= '0' && cp <= '9' || cp >= 'A' && cp <= 'F' || cp >= 'a' && cp <= 'f';
	}

	private static boolean isWhitespace(int cp) {
		return cp == ' ' || cp >= '\t' && cp <= '\r';
	}

	// ====

	@Nonnull
	private Token parseHeaderName(boolean isQuote) throws IOException, PreprocessorException {
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
	private Token parseNumber(@Nonnull String open) throws IOException, PreprocessorException {
		// Note: the open digit (or dot digit) are already consumed
		final StringBuilder text = new StringBuilder(open);
		while (true) {
			final int c = reader.read();
			if (c == '.' || isDigit(c) || isIdentifierNonDigit(c) || isIdentifierUCN(c)) {
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
	private Token parseIdentifierOrKeyword(@Nonnull String open)
			throws IOException, PreprocessorException {
		// open codepoint is already checked for digits or non identifier start UCN
		final StringBuilder text = new StringBuilder(open);
		while (true) {
			final int c = reader.read();
			if (isDigit(c) || isIdentifierNonDigit(c) || isIdentifierUCN(c)) {
				text.appendCodePoint(c);
			} else {
				if (c >= 0) reader.unread();
				// NOTE: alternative token
				// NOTE: in C this is a macro, not a internal token
				final String string = text.toString();
				switch (string) {
					case "and":
						return createToken(TokenType.AND_AND_ALT);
					case "and_eq":
						return createToken(TokenType.AND_ASSIGN_ALT);
					//noinspection SpellCheckingInspection
					case "bitand":
						return createToken(TokenType.AND_ALT);
					//noinspection SpellCheckingInspection
					case "bitor":
						return createToken(TokenType.OR_ALT);
					//noinspection SpellCheckingInspection
					case "compl":
						return createToken(TokenType.TILDE_ALT);
					case "not":
						return createToken(TokenType.NOT_ALT);
					case "not_eq":
						return createToken(TokenType.NOT_EQUAL_ALT);
					case "or":
						return createToken(TokenType.OR_OR_ALT);
					case "or_eq":
						return createToken(TokenType.OR_ASSIGN_ALT);
					case "xor":
						return createToken(TokenType.XOR_ALT);
					case "xor_eq":
						return createToken(TokenType.XOR_ASSIGN_ALT);
				}
				return createToken(TokenType.IDENTIFIER, string);
			}
		}
	}

	@Nonnull
	private Token parseBlockComment() throws IOException, PreprocessorException {
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
	private Token parseLineComment() throws IOException, PreprocessorException {
		// Note: both the forward slash "//" are already consumed
		final StringBuilder text = new StringBuilder("//");
		while (true) {
			final int c = reader.read();
			if (c == -1 || c == '\n') break;
			text.appendCodePoint(c);
		}
		return createToken(TokenType.LINE_COMMENT, text.toString());
	}

	private Token parseCharacterOrString(@Nonnull String open, boolean isChar)
			throws PreprocessorException, IOException {
		// Note: the open quote are already consumed
		final int close = isChar ? '\'' : '"';
		final StringBuilder text = new StringBuilder(open);
		int escape = 0;
		while (true) {
			final int c = reader.read();
			if (c == -1 || c == '\n') {
				postError(isChar ? "Unterminated character literal" : "Unterminated string literal");
				break;
			}
			text.appendCodePoint(c);
			if (escape == 0) {
				if (c == '\\') {
					escape = 1;
				} else if (c == close) {
					break;
				}
			} else if (escape == 1) {
				escape = c == 'x' ? -1 : 0;
			} else {
				if (!isHexadecimalDigit(c)) postError("\\x used with no following hex digits");
				escape = 0;
			}
		}
		return createToken(isChar ? TokenType.CHARACTER : TokenType.STRING, text.toString());
	}

	@Nonnull
	private Token parseRawString(@Nonnull String open) throws IOException, PreprocessorException {
		try {
			reader.setRawStringMode(true);
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
		} finally {
			reader.setRawStringMode(false);
		}
	}

	@Nonnull
	private Token parseWhitespaceOrNewline(int openCP) throws PreprocessorException, IOException {
		final StringBuilder text = new StringBuilder().appendCodePoint(openCP);
		boolean isNewline = openCP == '\r' || openCP == '\n';
		while (true) {
			final int c = reader.read();
			if (isWhitespace(c)) {
				text.appendCodePoint(c);
				isNewline |= c == '\r' || c == '\n';
			} else {
				if (c >= 0) reader.unread();
				return createToken(isNewline ? TokenType.NEW_LINE : TokenType.WHITESPACE, text.toString());
			}
		}
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
	private Token createToken(@Nonnull TokenType type) {
		return new Token(type, line, column);
	}

	@Nonnull
	private Token createToken(@Nonnull TokenType type, @Nonnull String text) {
		return new Token(type, line, column, text);
	}

	// ====

	@Nonnull
	private Token condition(int c, @Nonnull TokenType yes, @Nonnull TokenType no)
			throws PreprocessorException, IOException {
		// should not save line & column here, we are in the middle of a new token
		final int d = reader.read();
		if (c == d) return createToken(yes);
		reader.unread();
		return createToken(no);
	}

	@Nonnull
	private Token conditionDual(int c1, @Nonnull TokenType y1,
			int c2, @Nonnull TokenType y2, @Nonnull TokenType no)
			throws PreprocessorException, IOException {
		// should not save line & column here, we are in the middle of a new token
		final int d = reader.read();
		if (c1 == d) return createToken(y1);
		if (c2 == d) return createToken(y2);
		reader.unread();
		return createToken(no);
	}

	@Nullable
	private Token prefixCharacterOrString(@Nonnull String open)
			throws IOException, PreprocessorException {
		switch (reader.read()) {
			case '\'':
				return parseCharacterOrString(open + '\'', true);
			case '"':
				return parseCharacterOrString(open + '"', false);
			case 'R':
				if (reader.read() == '"') return parseRawString(open + "R\"");
				reader.unread();
		}
		reader.unread();
		return null;
	}

	@Nonnull
	@Override
	public Token nextToken() throws IOException, PreprocessorException {
		final int c = readAndSaveLineColumn();
		switch (c) {
			case -1:
				return createToken(TokenType.EOF);
			case '(':
				return createToken(TokenType.PAREN_OPEN);
			case ')':
				return createToken(TokenType.PAREN_CLOSE);
			case ',':
				return createToken(TokenType.COMMA);
			case ';':
				return createToken(TokenType.SEMI);
			case '?':
				return createToken(TokenType.QUESTION);
			case '[':
				return createToken(TokenType.BRACKET_OPEN);
			case ']':
				return createToken(TokenType.BRACKET_CLOSE);
			case '{':
				return createToken(TokenType.BRACE_OPEN);
			case '}':
				return createToken(TokenType.BRACE_CLOSE);
			case '~':
				return createToken(TokenType.TILDE);
			case '!':
				return condition('=', TokenType.NOT_EQUAL, TokenType.NOT);
			case '#':
				return condition('#', TokenType.PASTE, TokenType.HASH);
			case '+':
				return conditionDual('+', TokenType.PLUS_PLUS, '=', TokenType.PLUS_ASSIGN, TokenType.PLUS);
			case '-':
				switch (reader.read()) {
					case '-':
						return createToken(TokenType.MINUS_MINUS);
					case '=':
						return createToken(TokenType.MINUS_ASSIGN);
					case '>':
						return condition('*', TokenType.ARROW_STAR, TokenType.ARROW);
				}
				reader.unread();
				return createToken(TokenType.MINUS);
			case '*':
				return condition('=', TokenType.STAR_ASSIGN, TokenType.STAR);
			case '/':
				switch (reader.read()) {
					case '*':
						return parseBlockComment();
					case '/':
						return parseLineComment();
					case '=':
						return createToken(TokenType.DIV_ASSIGN);
				}
				reader.unread();
				return createToken(TokenType.DIV);
			case '%':
				switch (reader.read()) {
					case '=':
						return createToken(TokenType.MOD_ASSIGN);
					case '>':
						// NOTE: digraph
						return createToken(TokenType.BRACE_CLOSE_ALT);
					case ':':
						// NOTE: digraph
						if (reader.read() == '%') {
							if (reader.read() == ':') return createToken(TokenType.PASTE_ALT);
							reader.unread();
						}
						reader.unread();
						return createToken(TokenType.HASH_ALT);
				}
				reader.unread();
				return createToken(TokenType.MOD);
			case ':':
				// NOTE: digraph
				return conditionDual('>', TokenType.BRACKET_CLOSE_ALT, ':', TokenType.COLON_COLON, TokenType.COLON);
			case '<':
				if (isInclude) return parseHeaderName(false);
				switch (reader.read()) {
					case '=':
						return createToken(TokenType.LESS_EQUAL);
					case '<':
						return condition('=', TokenType.LEFT_SHIFT_ASSIGN, TokenType.LEFT_SHIFT);
					case '%':
						// NOTE: digraph
						return createToken(TokenType.BRACE_OPEN_ALT);
					case ':':
						// NOTE: digraph
						if (reader.read() == ':') {
							final int d = reader.read();
							if (d != ':' && d != '>') {
								reader.unreadMany(2);
								break;
							}
							reader.unread();
						}
						reader.unread();
						return createToken(TokenType.BRACKET_OPEN_ALT);
				}
				reader.unread();
				return createToken(TokenType.LESS);
			case '=':
				return condition('=', TokenType.EQUAL, TokenType.ASSIGN);
			case '>':
				switch (reader.read()) {
					case '=':
						return createToken(TokenType.GREATER_EQUAL);
					case '>':
						return condition('=', TokenType.RIGHT_SHIFT_ASSIGN, TokenType.RIGHT_SHIFT);
				}
				reader.unread();
				return createToken(TokenType.GREATER);
			case '^':
				return condition('=', TokenType.XOR_ASSIGN, TokenType.XOR);
			case '|':
				return conditionDual('=', TokenType.OR_ASSIGN, '|', TokenType.OR_OR, TokenType.OR);
			case '&':
				return conditionDual('=', TokenType.AND_ASSIGN, '&', TokenType.AND_AND, TokenType.AND);
			case '.': {
				final int d = reader.read();
				if (isDigit(d)) return parseNumber("." + Character.toString(d));
				if (d == '*') return createToken(TokenType.DOT_STAR);
				if (d == '.') {
					if (reader.read() == '.') return createToken(TokenType.ELLIPSIS);
					reader.unread();
				}
				reader.unread();
				return createToken(TokenType.DOT);
			}
			case '\'':
				return parseCharacterOrString("'", true);
			case 'u': {
				if (reader.read() == '8') {
					final Token token = prefixCharacterOrString("u8");
					if (token != null) return token;
					return parseIdentifierOrKeyword("u8");
				}
				reader.unread();
				final Token token = prefixCharacterOrString("u");
				if (token != null) return token;
				return parseIdentifierOrKeyword("u");
			}
			case 'U': {
				final Token token = prefixCharacterOrString("U");
				if (token != null) return token;
				return parseIdentifierOrKeyword("U");
			}
			case 'L': {
				final Token token = prefixCharacterOrString("L");
				if (token != null) return token;
				return parseIdentifierOrKeyword("L");
			}
			case 'R':
				if (reader.read() == '"') return parseRawString("R\"");
				reader.unread();
				return parseIdentifierOrKeyword("R");
			case '"':
				return isInclude ? parseHeaderName(true) : parseCharacterOrString("\"", false);
		}
		if (isDigit(c)) {
			return parseNumber(Character.toString(c));
		} else if (isIdentifierNonDigit(c) || isIdentifierInitialUCN(c)) {
			return parseIdentifierOrKeyword(Character.toString(c));
		} else if (isWhitespace(c)) {
			final Token token = parseWhitespaceOrNewline(c);
			this.isNewline = token.getType() == TokenType.NEW_LINE;
			return token;
		}
		postError("Unknown character");
		return createToken(TokenType.UNKNOWN, Character.toString(c));
	}
}
