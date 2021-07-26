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

	LEFT_SHIFT(false, "<<"),
	RIGHT_SHIFT(false, ">>"),
	LESS_EQUAL(false, "<="),
	GREATER_EQUAL(false, ">="),
	EQUAL(false, "=="),
	NOT_EQUAL(false, "!="),
	AND_AND(false, "&&"),
	OR_OR(false, "||"),
	STAR_ASSIGN(false, "*="),
	DIV_ASSIGN(false, "/="),
	MOD_ASSIGN(false, "%="),
	PLUS_ASSIGN(false, "+="),
	MINUS_ASSIGN(false, "-="),
	AND_ASSIGN(false, "&="),
	XOR_ASSIGN(false, "^="),
	OR_ASSIGN(false, "|="),
	LEFT_SHIFT_ASSIGN(false, "<<="),
	RIGHT_SHIFT_ASSIGN(false, ">>="),

	HASH(false, "#"),
	PASTE(false, "##"),

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
