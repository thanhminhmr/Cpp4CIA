/*
 * Anarres C Preprocessor
 * Copyright (c) 2007-2015, Shevek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.anarres.cpp;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.util.Objects;

/**
 * A Preprocessor token.
 *
 * @see Preprocessor
 */
public final class Token {
	private final int type;
	private final int line;
	private final int column;
	@Nonnull private final String text;
	@Nullable private final Object value;

	public Token(int type, int line, int column, @Nonnull String text, @Nullable Object value) {
		this.type = type;
		this.line = line;
		this.column = column;
		this.text = text;
		this.value = value;
	}

	public Token(int type, int line, int column, @Nonnull String text) {
		this(type, line, column, text, null);
	}

	public Token(int type, int line, int column) {
		this(type, line, column, Objects.requireNonNull(getDefaultString(type)), null);
	}

	@Nonnull
	static StringBuilder escape(@Nonnull StringBuilder builder, @Nonnull CharSequence charSequence) {
		for (final int codePoint : charSequence.codePoints().toArray()) {
			if (codePoint == '\\') {
				builder.append("\\\\");
			} else if (codePoint == '"') {
				builder.append("\\\"");
			} else if (codePoint == '\n') {
				builder.append("\\n");
			} else if (codePoint == '\r') {
				builder.append("\\r");
			} else {
				builder.appendCodePoint(codePoint);
			}
		}
		return builder;
	}

	/**
	 * Returns the semantic type of this token.
	 *
	 * @return the semantic type of this token.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns the line at which this token started.
	 * <p>
	 * Lines are numbered from 1.
	 *
	 * @return the line at which this token started.
	 * @see LexerSource#getLine()
	 */
	public int getLine() {
		return line;
	}

	/**
	 * Returns the column at which this token started.
	 * <p>
	 * Columns are numbered from 0.
	 *
	 * @return the column at which this token started.
	 * @see LexerSource#getColumn()
	 */
	public int getColumn() {
		return column;
	}

	/**
	 * Returns the original or generated text of this token.
	 * <p>
	 * This is distinct from the semantic value of the token.
	 *
	 * @return the original or generated text of this token.
	 * @see #getValue()
	 */
	@Nonnull
	public String getText() {
		return text;
	}

	/**
	 * Returns the semantic value of this token.
	 * <p>
	 * For strings, this is the parsed String.
	 * For integers, this is an Integer object.
	 * For other token types, as appropriate.
	 *
	 * @return the semantic value of this token, or null.
	 * @see #getText()
	 */
	@Nullable
	Object getValue() {
		return value;
	}

	@Nonnull
	<E> E getValue(@Nonnull Class<E> valueClass) throws NullPointerException, ClassCastException {
		assert valueClass.isInstance(value);
		return valueClass.cast(value);
	}

	/**
	 * Returns a description of this token, for debugging purposes.
	 */
	@Nonnull
	@Override
	public String toString() {
		return text;
	}

	public static final int PAREN_OPEN = '(';
	public static final int PAREN_CLOSE = ')';
	public static final int BRACKET_OPEN = '[';
	public static final int BRACKET_CLOSE = ']';
	public static final int BRACE_OPEN = '{';
	public static final int BRACE_CLOSE = '}';
	public static final int SEMI = ';';
	public static final int COMMA = ',';
	public static final int DOT = '.';
	public static final int NOT = '!';
	public static final int TILDE = '~';
	public static final int PLUS = '+';
	public static final int MINUS = '-';
	public static final int STAR = '*';
	public static final int DIV = '/';
	public static final int MOD = '%';
	public static final int AND = '&';
	public static final int OR = '|';
	public static final int XOR = '^';
	public static final int LESS = '<';
	public static final int GREATER = '>';
	public static final int QUESTION = '?';
	public static final int COLON = ':';
	public static final int ASSIGN = '=';
	public static final int HASH = '#';
	public static final int ARROW = 0x10001; // ->
	public static final int ELLIPSIS = 0x10002; // ...
	public static final int INCREASE = 0x10003; // ++
	public static final int DECREASE = 0x10004; // --
	public static final int AND_AND = 0x10005; // &&
	public static final int OR_OR = 0x10006; // ||
	public static final int LEFT_SHIFT = 0x10007; // <<
	public static final int RIGHT_SHIFT = 0x10008; // >>
	public static final int ADD_ASSIGN = 0x10009; // +=
	public static final int SUB_ASSIGN = 0x1000A; // -=
	public static final int MUL_ASSIGN = 0x1000B; // *=
	public static final int DIV_ASSIGN = 0x1000C; // /=
	public static final int MOD_ASSIGN = 0x1000D; // %=
	public static final int AND_ASSIGN = 0x1000E; // &=
	public static final int XOR_ASSIGN = 0x1000F; // ^=
	public static final int OR_ASSIGN = 0x10010; // |=
	public static final int LSH_ASSIGN = 0x10011; // <<=
	public static final int RSH_ASSIGN = 0x10012; // >>=
	public static final int EQUAL = 0x10013; // ==
	public static final int NOT_EQUAL = 0x10014; // !=
	public static final int LESS_EQUAL = 0x10015; // <=
	public static final int GREATER_EQUAL = 0x10016; // >=
	public static final int IDENTIFIER = 0x10017;
	public static final int CHARACTER = 0x10018;
	public static final int NUMBER = 0x10019;
	public static final int STRING = 0x1001A;
	public static final int HEADER = 0x1001B;
	public static final int NEW_LINE = 0x1001C;
	public static final int WHITESPACE = 0x1001D;
	public static final int C_COMMENT = 0x1001E;
	public static final int CPP_COMMENT = 0x1001F;
	public static final int EOF = 0x10020;
	public static final int M_ARG = 0x10021;
	public static final int M_OPT = 0x10022;
	public static final int M_PASTE = 0x10023;
	public static final int M_STRING = 0x10024;
	public static final int P_LINE = 0x10025;
	public static final int P_HASH = 0x10026; // #
	public static final int P_PASTE = 0x10027; // ##
	public static final int INVALID = 0x10028;

	/**
	 * The position-less space token.
	 */
	static final Token whitespace = new Token(WHITESPACE, -1, -1, " ");
	static final Token eof = new Token(EOF, -1, -1, "");

	@Nullable
	private static String getDefaultString(int token) {
		switch (token) {
			case PAREN_OPEN:
			case PAREN_CLOSE:
			case BRACKET_OPEN:
			case BRACKET_CLOSE:
			case BRACE_OPEN:
			case BRACE_CLOSE:
			case SEMI:
			case COMMA:
			case DOT:
			case NOT:
			case TILDE:
			case PLUS:
			case MINUS:
			case STAR:
			case DIV:
			case MOD:
			case AND:
			case OR:
			case XOR:
			case LESS:
			case GREATER:
			case QUESTION:
			case COLON:
			case ASSIGN:
			case HASH:
				return Character.toString(token);

			case ARROW:
				return "->";
			case ELLIPSIS:
				return "...";
			case INCREASE:
				return "++";
			case DECREASE:
				return "--";
			case AND_AND:
				return "&&";
			case OR_OR:
				return "||";
			case LEFT_SHIFT:
				return "<<";
			case RIGHT_SHIFT:
				return ">>";
			case ADD_ASSIGN:
				return "+=";
			case SUB_ASSIGN:
				return "-=";
			case MUL_ASSIGN:
				return "*=";
			case DIV_ASSIGN:
				return "/=";
			case MOD_ASSIGN:
				return "%=";
			case AND_ASSIGN:
				return "&=";
			case XOR_ASSIGN:
				return "^=";
			case OR_ASSIGN:
				return "|=";
			case LSH_ASSIGN:
				return "<<=";
			case RSH_ASSIGN:
				return ">>=";
			case EQUAL:
				return "==";
			case NOT_EQUAL:
				return "!=";
			case LESS_EQUAL:
				return "<=";
			case GREATER_EQUAL:
				return ">=";

			case P_HASH:
				return "#";
			case P_PASTE:
				return "##";

			case EOF:
				return "";

			default:
				return null;
		}
	}

}
