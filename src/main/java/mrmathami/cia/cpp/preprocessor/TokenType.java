package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;

public enum TokenType {
	/* Simple token */
	PAREN_OPEN(false, "("),
	PAREN_CLOSE(false, ")"),

	BRACKET_OPEN(false, "["),
	BRACKET_CLOSE(false, "]"),
	BRACE_OPEN(false, "{"),
	BRACE_CLOSE(false, "}"),

	BRACKET_OPEN_ALT(false, "<:"),
	BRACKET_CLOSE_ALT(false, ":>"),
	BRACE_OPEN_ALT(false, "<%"),
	BRACE_CLOSE_ALT(false, "%>"),

	SEMI(false, ";"),
	COMMA(false, ","),
	DOT(false, "."),
	NOT(false, "!"),
	TILDE(false, "~"),
	PLUS(false, "+"),
	MINUS(false, "-"),
	STAR(false, "*"),
	DIV(false, "/"),
	MOD(false, "%"),
	AND(false, "&"),
	OR(false, "|"),
	XOR(false, "^"),
	LESS(false, "<"),
	GREATER(false, ">"),
	QUESTION(false, "?"),
	COLON(false, ":"),
	ASSIGN(false, "="),

	NOT_ALT(false, "not"),
	TILDE_ALT(false, "compl"),
	AND_ALT(false, "bitand"),
	OR_ALT(false, "bitor"),
	XOR_ALT(false, "xor"),

	DOT_STAR(false, ".*"),
	COLON_COLON(false, "::"),

	PLUS_PLUS(false, "++"),
	MINUS_MINUS(false, "--"),

	ARROW(false, "->"),
	ARROW_STAR(false, "->*"),

	LEFT_SHIFT(false, "<<"),
	RIGHT_SHIFT(false, ">>"),
	LESS_EQUAL(false, "<="),
	GREATER_EQUAL(false, ">="),
	EQUAL(false, "=="),
	NOT_EQUAL(false, "!="),

	NOT_EQUAL_ALT(false, "not_eq"),

	AND_AND(false, "&&"),
	OR_OR(false, "||"),

	AND_AND_ALT(false, "and"),
	OR_OR_ALT(false, "or"),

	STAR_ASSIGN(false, "*="),
	DIV_ASSIGN(false, "/="),
	MOD_ASSIGN(false, "%="),
	PLUS_ASSIGN(false, "+="),
	MINUS_ASSIGN(false, "-="),
	AND_ASSIGN(false, "&="),
	XOR_ASSIGN(false, "^="),
	OR_ASSIGN(false, "|="),

	AND_ASSIGN_ALT(false, "and_eq"),
	XOR_ASSIGN_ALT(false, "xor_eq"),
	OR_ASSIGN_ALT(false, "or_eq"),

	LEFT_SHIFT_ASSIGN(false, "<<="),
	RIGHT_SHIFT_ASSIGN(false, ">>="),

	ELLIPSIS(false, "..."),
	HASH(false, "#"),
	PASTE(false, "##"),

	HASH_ALT(false, "%:"),
	PASTE_ALT(false, "%:%:"),

	EOF(false, ""),

	/* Text container token */
	NEW_LINE(true, "<NEW_LINE>"),
	WHITESPACE(true, "<WHITESPACE>"),

	BLOCK_COMMENT(true, "<BLOCK_COMMENT>"),
	LINE_COMMENT(true, "<LINE_COMMENT>"),

	NUMBER(true, "<NUMBER>"),
	CHARACTER(true, "<CHARACTER>"),
	STRING(true, "<STRING>"),
	HEADER_NAME(true, "<HEADER_NAME>"),

	IDENTIFIER(true, "<IDENTIFIER>"),

	UNKNOWN(true, "<UNKNOWN>");

	private final boolean textContainer;
	@Nonnull private final String text;

	TokenType(boolean textContainer, @Nonnull String text) {
		this.textContainer = textContainer;
		this.text = text;
	}

	public boolean isTextContainer() {
		return textContainer;
	}

	@Override
	public final String toString() {
		return text;
	}
}
