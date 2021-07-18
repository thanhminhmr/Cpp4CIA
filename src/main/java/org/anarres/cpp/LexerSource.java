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

import java.io.IOException;
import java.io.Reader;

import static org.anarres.cpp.Token.AND_EQ;
import static org.anarres.cpp.Token.ARROW;
import static org.anarres.cpp.Token.CHARACTER;
import static org.anarres.cpp.Token.CPP_COMMENT;
import static org.anarres.cpp.Token.C_COMMENT;
import static org.anarres.cpp.Token.DEC;
import static org.anarres.cpp.Token.DIV_EQ;
import static org.anarres.cpp.Token.ELLIPSIS;
import static org.anarres.cpp.Token.EOF;
import static org.anarres.cpp.Token.EQ;
import static org.anarres.cpp.Token.GE;
import static org.anarres.cpp.Token.HASH;
import static org.anarres.cpp.Token.HEADER;
import static org.anarres.cpp.Token.IDENTIFIER;
import static org.anarres.cpp.Token.INC;
import static org.anarres.cpp.Token.INVALID;
import static org.anarres.cpp.Token.LAND;
import static org.anarres.cpp.Token.LAND_EQ;
import static org.anarres.cpp.Token.LE;
import static org.anarres.cpp.Token.LOR;
import static org.anarres.cpp.Token.LOR_EQ;
import static org.anarres.cpp.Token.LSH;
import static org.anarres.cpp.Token.LSH_EQ;
import static org.anarres.cpp.Token.MOD_EQ;
import static org.anarres.cpp.Token.MULT_EQ;
import static org.anarres.cpp.Token.NE;
import static org.anarres.cpp.Token.NL;
import static org.anarres.cpp.Token.NUMBER;
import static org.anarres.cpp.Token.OR_EQ;
import static org.anarres.cpp.Token.PASTE;
import static org.anarres.cpp.Token.PLUS_EQ;
import static org.anarres.cpp.Token.RANGE;
import static org.anarres.cpp.Token.RSH;
import static org.anarres.cpp.Token.RSH_EQ;
import static org.anarres.cpp.Token.STRING;
import static org.anarres.cpp.Token.SUB_EQ;
import static org.anarres.cpp.Token.WHITESPACE;
import static org.anarres.cpp.Token.XOR_EQ;

/**
 * Does not handle digraphs.
 */
public class LexerSource extends Source {
	private static final String VALID_SEPARATOR_CHAR
			= "!\"#%&'*+,-./0123456789:;<=>?ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_abcdefghijklmnopqrstuvwxyz{|}~";

	private JoinReader reader;
	private final boolean ppvalid;
	private boolean isStartOfLine;
	private boolean isInclude;

	private boolean digraphs;

	/* Unread. */
	private int u0, u1, u2, u3;
	private int ucount;

	private int line;
	private int column;
	private int lastColumn;
	private boolean cr;


	private int markLine;
	private int markColumn;

	/* ppvalid is:
	 * false in StringLexerSource,
	 * true in FileLexerSource */
	public LexerSource(Reader r, boolean ppvalid) {
		this.reader = new JoinReader(r);
		this.ppvalid = ppvalid;
		this.isStartOfLine = true;
		this.isInclude = false;

		this.digraphs = true;

		this.ucount = 0;

		this.line = 1;
		this.column = 0;
		this.lastColumn = -1;
		this.cr = false;
	}

	@Override
	void init(Preprocessor pp) {
		super.init(pp);
		this.digraphs = pp.getFeature(Feature.DIGRAPHS);
		this.reader.init(pp, this);
	}

	/**
	 * Returns the line number of the last read character in this source.
	 * <p>
	 * Lines are numbered from 1.
	 *
	 * @return the line number of the last read character in this source.
	 */
	@Override
	public int getLine() {
		return line;
	}

	/**
	 * Returns the column number of the last read character in this source.
	 * <p>
	 * Columns are numbered from 0.
	 *
	 * @return the column number of the last read character in this source.
	 */
	@Override
	public int getColumn() {
		return column;
	}

	@Override
	boolean isNumbered() {
		return true;
	}

	/* Error handling. */
	private void _error(String msg, boolean error)
			throws LexerException {
		int line = this.line;
		int column = this.column;
		if (column == 0) {
			column = lastColumn;
			line--;
		} else {
			column--;
		}
		if (error)
			super.error(line, column, msg);
		else
			super.warning(line, column, msg);
	}

	/* Allow JoinReader to call this. */
	final void error(String msg) throws LexerException {
		_error(msg, true);
	}

	/* Allow JoinReader to call this. */
	final void warning(String msg) throws LexerException {
		_error(msg, false);
	}

	/* A flag for string handling. */

	void setInclude(boolean b) {
		this.isInclude = b;
	}

	/*
	 * private boolean _isLineSeparator(int c) {
	 * return Character.getType(c) == Character.LINE_SEPARATOR
	 * || c == -1;
	 * }
	 */

	/* XXX Move to JoinReader and canonicalise newlines. */
	private static boolean isLineSeparator(int c) {
		switch ((char) c) {
			case '\r':
			case '\n':
			case '\u2028':
			case '\u2029':
			case '\u000B':
			case '\u000C':
			case '\u0085':
				return true;
			default:
				return (c == -1);
		}
	}

	private int _read() throws IOException, LexerException {
		assert ucount >= 0 && ucount <= 4 : "Illegal ucount: " + ucount;
		return ucount <= 0 ? reader != null ? reader.read() : -1
				: ucount <= 2 ? ucount-- == 1 ? u0 : u1 : ucount-- == 3 ? u2 : u3;
	}

	private int read() throws IOException, LexerException {
		int c = _read();
		if (c == '\r') {
			this.cr = true;
			this.line++;
			this.lastColumn = column;
			this.column = 0;
		} else if (c == '\n') {
			if (cr) {
				this.cr = false;
				return c;
			}
			this.cr = false;
			this.line++;
			this.lastColumn = column;
			this.column = 0;
		} else if (c == '\u2028' || c == '\u2029' || c == '\u000B' || c == '\u000C' || c == '\u0085') {
			this.cr = false;
			this.line++;
			this.lastColumn = column;
			this.column = 0;
		} else if (c == -1) {
			this.cr = false;
		} else {
			this.cr = false;
			this.column++;
		}
		return c;
	}

	private void _unread(int c) {
		if (ucount < 4) {
			if (ucount == 0) {
				u0 = c;
			} else if (ucount == 1) {
				u1 = c;
			} else if (ucount == 2) {
				u2 = c;
			} else if (ucount == 3) {
				u3 = c;
			}
			ucount++;
		} else {
			throw new IllegalStateException("Cannot unget another character!");
		}
	}

	/* You can unget AT MOST one newline. */
	private void unread(int c) {
		/* XXX Must unread newlines. */
		if (c != -1) {
			if (isLineSeparator(c)) {
				line--;
				column = lastColumn;
				cr = false;
			} else {
				column--;
			}
			_unread(c);
		}
	}


	@Nonnull
	private Token cComment() throws IOException, LexerException {
		final StringBuilder text = new StringBuilder("/*");
		int d;
		do {
			do {
				d = read();
				if (d == -1)
					return _marked_token(INVALID, text.toString(), "Unterminated comment");
				text.append((char) d);
			} while (d != '*');
			do {
				d = read();
				if (d == -1)
					return _marked_token(INVALID, text.toString(), "Unterminated comment");
				text.append((char) d);
			} while (d == '*');
		} while (d != '/');
		return _marked_token(C_COMMENT, text.toString());
	}

	@Nonnull
	private Token cppComment() throws IOException, LexerException {
		final StringBuilder text = new StringBuilder("//");
		int d = read();
		while (!isLineSeparator(d)) {
			text.append((char) d);
			d = read();
		}
		unread(d);
		return _marked_token(CPP_COMMENT, text.toString());
	}

	/**
	 * Lexes an escaped character, appends the lexed escape sequence to 'text' and returns the parsed character value.
	 *
	 * @param text The buffer to which the literal escape sequence is appended.
	 * @return The new parsed character value.
	 * @throws IOException    if it goes badly wrong.
	 * @throws LexerException if it goes wrong.
	 */
	private int escape(StringBuilder text) throws IOException, LexerException {
		final int c = read();
		if (c == 'a') {
			text.append('a');
			return 0x07;
		} else if (c == 'b') {
			text.append('b');
			return '\b';
		} else if (c == 'f') {
			text.append('f');
			return '\f';
		} else if (c == 'n') {
			text.append('n');
			return '\n';
		} else if (c == 'r') {
			text.append('r');
			return '\r';
		} else if (c == 't') {
			text.append('t');
			return '\t';
		} else if (c == 'v') {
			text.append('v');
			return 0x0b;
		} else if (c == '\\') {
			text.append('\\');
			return '\\';
		} else if (c >= '0' && c <= '7') {
			text.append((char) c);
			int val = Character.digit(c, 8);
			final int c2 = read();
			final int d2 = Character.digit(c2, 8);
			if (d2 >= 0) {
				text.append((char) c2);
				val = (val << 3) | d2;
				final int c3 = read();
				final int d3 = Character.digit(c3, 8);
				if (d3 >= 0) {
					text.append((char) c3);
					val = (val << 3) | d3;
				} else {
					unread(c3);
				}
			} else {
				unread(c2);
			}
			return val;
		} else if (c == 'x') {
			text.append((char) c);
			int val = 0;
			final int c1 = read();
			final int d1 = Character.digit(c, 16);
			if (d1 >= 0) {
				text.append((char) c1);
				val = d1;
				while (true) {
					final int cc = read();
					final int dd = Character.digit(cc, 16);
					if (dd < 0) {
						unread(cc);
						return val;
					}
					text.append((char) cc);
					val = (val << 4) | dd;
				}
			} else {
				unread(c1);
				warning("Expect hex digit on hex escape sequence, found " + (char) c1);
			}
			return val;

			/* Exclude two cases from the warning. */
		} else if (c == '"') {
			text.append('"');
			return '"';
		} else if (c == '\'') {
			text.append('\'');
			return '\'';
		}
		warning("Unnecessary escape character " + (char) c);
		text.append((char) c);
		return c;
	}

	@Nonnull
	private Token string(String open, boolean isSingleQuote) throws IOException, LexerException {
		final StringBuilder text = new StringBuilder(open);
		final StringBuilder value = new StringBuilder();
		while (true) {
			int c = read();
			if (c == -1) {
				return _marked_token(INVALID, text.toString(), "End of file in string literal after: " + value);
			} else if (isLineSeparator(c)) {
				unread(c);
				return _marked_token(INVALID, text.toString(), "Unterminated string literal after: " + value);
			}
			text.append((char) c);
			if (c == '\'' && isSingleQuote || c == '"' && !isSingleQuote) {
				return _marked_token(isSingleQuote ? CHARACTER : STRING, text.toString(), value.toString());
			}
			value.append((char) (c != '\\' ? c : escape(text)));
		}
	}

	@Nonnull
	private Token raw_string(String open) throws IOException, LexerException {
		try {
			reader.setRawString(true);
			final StringBuilder text = new StringBuilder(open);
			final StringBuilder separator = new StringBuilder();
			while (true) {
				int c = read();
				if (c == -1) return _marked_token(INVALID, text.toString(), "End of file in raw string literal");
				text.append((char) c);
				if (c == '(') break;
				if (VALID_SEPARATOR_CHAR.indexOf(c) < 0) {
					throw new LexerException("Invalid raw string: invalid character in separator!");
				}
				separator.append((char) c);
			}

			final StringBuilder value = new StringBuilder();
			final StringBuilder temp = new StringBuilder();
			while (true) {
				int c = read();
				if (c == -1) {
					return _marked_token(INVALID, text.toString(), "End of file in raw string literal");
				}
				text.append((char) c);
				if (c == ')') {
					while (true) {
						int d = read();
						if (d == -1)
							return _marked_token(INVALID, text.toString(), "End of file in raw string literal");
						text.append((char) d);
						if (d == '"' && temp.compareTo(separator) == 0) {
							return _marked_token(STRING, text.toString(), value.toString());
						}
						temp.append((char) d);
						if (VALID_SEPARATOR_CHAR.indexOf(d) < 0) {
							value.append(temp);
							temp.setLength(0);
							if (d != ')') break;
						}
					}
				} else {
					value.append((char) c);
				}
			}
		} finally {
			reader.setRawString(false);
		}
	}

	@Nonnull
	private Token header(boolean isQuote) throws IOException, LexerException {
		final StringBuilder text = new StringBuilder(isQuote ? "\"" : "<");
		final StringBuilder value = new StringBuilder();
		while (true) {
			int c = read();
			if (c == -1) {
				return _marked_token(INVALID, text.toString(), "End of file in header name");
			} else if (isLineSeparator(c)) {
				unread(c);
				return _marked_token(INVALID, text.toString(), "Unterminated string literal");
			}
			text.append((char) c);
			if (c == '"' && isQuote || c == '>' && !isQuote) {
				return _marked_token(HEADER, text.toString(), value.toString());
			}
			value.append((char) c);
		}
	}

	@Nonnull
	private Token _number_suffix(StringBuilder text, NumberToken value, int d)
			throws IOException,
			LexerException {
		int flags = 0;    // U, I, L, LL, F, D, MSB
		while (true) {
			if (d == 'U' || d == 'u') {
				if ((flags & NumberToken.F_UNSIGNED) != 0)
					warning("Duplicate unsigned suffix " + d);
				flags |= NumberToken.F_UNSIGNED;
				text.append((char) d);
				d = read();
			} else if (d == 'L' || d == 'l') {
				if ((flags & NumberToken.FF_SIZE) != 0)
					warning("Multiple length suffixes after " + text);
				text.append((char) d);
				int e = read();
				if (e == d) {    // Case must match. Ll is Welsh.
					flags |= NumberToken.F_LONG_LONG;
					text.append((char) e);
					d = read();
				} else {
					flags |= NumberToken.F_LONG;
					d = e;
				}
			} else if (d == 'I' || d == 'i') {
				if ((flags & NumberToken.FF_SIZE) != 0)
					warning("Multiple length suffixes after " + text);
				flags |= NumberToken.F_INT;
				text.append((char) d);
				d = read();
			} else if (d == 'F' || d == 'f') {
				if ((flags & NumberToken.FF_SIZE) != 0)
					warning("Multiple length suffixes after " + text);
				flags |= NumberToken.F_FLOAT;
				text.append((char) d);
				d = read();
			} else if (d == 'D' || d == 'd') {
				if ((flags & NumberToken.FF_SIZE) != 0)
					warning("Multiple length suffixes after " + text);
				flags |= NumberToken.F_DOUBLE;
				text.append((char) d);
				d = read();
			} else if (Character.isUnicodeIdentifierPart(d)) {
				String reason = "Invalid suffix \"" + (char) d + "\" on numeric constant";
				// We've encountered something initially identified as a number.
				// Read in the rest of this token as an identifer but return it as an invalid.
				while (Character.isUnicodeIdentifierPart(d)) {
					text.append((char) d);
					d = read();
				}
				unread(d);
				return _marked_token(INVALID, text.toString(), reason);
			} else {
				unread(d);
				value.setFlags(flags);
				return _marked_token(NUMBER, text.toString(), value);
			}
		}
	}

	/* Either a decimal part, or a hex exponent. */
	@Nonnull
	private String _number_part(StringBuilder text, int base, boolean sign) throws IOException, LexerException {
		StringBuilder part = new StringBuilder();
		int d = read();
		if (sign && d == '-') {
			text.append((char) d);
			part.append((char) d);
			d = read();
		}
		while (Character.digit(d, base) != -1) {
			text.append((char) d);
			part.append((char) d);
			d = read();
		}
		unread(d);
		return part.toString();
	}

	/* We do not know whether the first digit is valid. */
	@Nonnull
	private Token number_hex_bin(int d, boolean hex) throws IOException, LexerException {
		final StringBuilder text = new StringBuilder("0").append((char) d);
		String integer = _number_part(text, hex ? 16 : 2, false);
		NumberToken value = new NumberToken(hex ? 16 : 2, integer);
		d = read();
		if (hex) {
			if (d == '.') {
				text.append((char) d);
				String fraction = _number_part(text, 16, false);
				value.setFractionalPart(fraction);
				d = read();
			}
			if (d == 'P' || d == 'p') {
				text.append((char) d);
				String exponent = _number_part(text, 10, true);
				value.setExponent(true, exponent);
				d = read();
			}
		}
		return _number_suffix(text, value, d);
	}

	/* We know we have at least one valid digit, but empty is not
	 * fine. */
	@Nonnull
	private Token number_decimal() throws IOException, LexerException {
		StringBuilder text = new StringBuilder();
		String integer = _number_part(text, 10, false);
		String fraction = null;
		String exponent = null;
		int d = read();
		if (d == '.') {
			text.append((char) d);
			fraction = _number_part(text, 10, false);
			d = read();
		}
		if (d == 'E' || d == 'e') {
			text.append((char) d);
			exponent = _number_part(text, 10, true);
			d = read();
		}
		int base = 10;
		if (fraction == null && exponent == null && integer.startsWith("0")) {
			if (integer.replaceAll("[0-7]+", "").isEmpty()) {
				base = 8;
			} else {
				warning("Decimal constant starts with 0, but not octal: " + integer);
			}
		}
		NumberToken value = new NumberToken(base, integer);
		if (fraction != null) value.setFractionalPart(fraction);
		if (exponent != null) value.setExponent(false, exponent);
		// XXX Make sure it's got enough parts
		return _number_suffix(text, value, d);
	}

	/**
	 * Section 6.4.4.1 of C99
	 * <p>
	 * (Not pasted here, but says that the initial negation is a separate token.)
	 * <p>
	 * Section 6.4.4.2 of C99
	 * <p>
	 * A floating constant has a significand part that may be followed
	 * by an exponent part and a suffix that specifies its type. The
	 * components of the significand part may include a digit sequence
	 * representing the whole-number part, followed by a period (.),
	 * followed by a digit sequence representing the fraction part.
	 * <p>
	 * The components of the exponent part are an e, E, p, or P
	 * followed by an exponent consisting of an optionally signed digit
	 * sequence. Either the whole-number part or the fraction part has to
	 * be present; for decimal floating constants, either the period or
	 * the exponent part has to be present.
	 * <p>
	 * The significand part is interpreted as a (decimal or hexadecimal)
	 * rational number; the digit sequence in the exponent part is
	 * interpreted as a decimal integer. For decimal floating constants,
	 * the exponent indicates the power of 10 by which the significand
	 * part is to be scaled. For hexadecimal floating constants, the
	 * exponent indicates the power of 2 by which the significand part is
	 * to be scaled.
	 * <p>
	 * For decimal floating constants, and also for hexadecimal
	 * floating constants when FLT_RADIX is not a power of 2, the result
	 * is either the nearest representable value, or the larger or smaller
	 * representable value immediately adjacent to the nearest representable
	 * value, chosen in an implementation-defined manner. For hexadecimal
	 * floating constants when FLT_RADIX is a power of 2, the result is
	 * correctly rounded.
	 */
	@Nonnull
	private Token number() throws IOException, LexerException {
		Token tok;
		int c = read();
		if (c == '0') {
			int d = read();
			if (d == 'x' || d == 'X' || d == 'b' || d == 'B') {
				tok = number_hex_bin(d, d == 'x' || d == 'X');
			} else {
				unread(d);
				unread(c);
				tok = number_decimal();
			}
		} else if (Character.isDigit(c) || c == '.') {
			unread(c);
			tok = number_decimal();
		} else {
			throw new LexerException("Asked to parse something as a number which isn't: " + (char) c);
		}
		return tok;
	}

	@Nonnull
	private Token identifier(int c) throws IOException, LexerException {
		final StringBuilder text = new StringBuilder().append((char) c);
		int d;
		while (true) {
			d = read();
			if (!Character.isIdentifierIgnorable(d)) {
				if (!Character.isJavaIdentifierPart(d)) break;
				text.append((char) d);
			}
		}
		unread(d);
		return _marked_token(IDENTIFIER, text.toString());
	}

	@Nonnull
	private Token whitespace(int c) throws IOException, LexerException {
		StringBuilder text = new StringBuilder();
		int d;
		text.append((char) c);
		while (true) {
			d = read();
			if (ppvalid && isLineSeparator(d) && !isStartOfLine) /* XXX Ugly. */
				break;
			if (Character.isWhitespace(d))
				text.append((char) d);
			else
				break;
		}
		unread(d);
		return _marked_token(WHITESPACE, text.toString());
	}

	/* No token processed by cond() contains a newline. */
	@Nonnull
	private Token cond(char c, int yes, int no) throws IOException, LexerException {
		int d = read();
		if (c == d) return _marked_token(yes);
		unread(d);
		return _marked_token(no);
	}

	/* No token processed by cond2() contains a newline. */
	@Nonnull
	private Token cond2(char c1, int y1, char c2, int y2, int no) throws IOException, LexerException {
		int d = read();
		if (c1 == d) return _marked_token(y1);
		if (c2 == d) return _marked_token(y2);
		unread(d);
		return _marked_token(no);
	}

	/* No token processed by cond3() contains a newline. */
	@Nonnull
	private Token cond3(char c1, int y1, char c2, int y2, char c3, int y3, int no) throws IOException, LexerException {
		int d = read();
		if (c1 == d) return _marked_token(y1);
		if (c2 == d) return _marked_token(y2);
		if (c3 == d) return _marked_token(y3);
		unread(d);
		return _marked_token(no);
	}

	@Nonnull
	private Token _marked_token(int type, String text, Object value) {
		assert markLine > 0 && markColumn > 0;
		return new Token(type, markLine, markColumn, text, value);
	}

	@Nonnull
	private Token _marked_token(int type, String text) {
		assert markLine > 0 && markColumn > 0;
		return new Token(type, markLine, markColumn, text);
	}

	@Nonnull
	private Token _marked_token(int type) {
		assert markLine > 0 && markColumn > 0;
		return new Token(type, markLine, markColumn);
	}

	private void _mark() {
		this.markLine = line;
		this.markColumn = column;
	}

	@Nullable
	private Token _token(int c) throws IOException, LexerException {
		if (c == '!') {
			return cond('=', NE, '!');
		} else if (c == '#') {
			return isStartOfLine ? _marked_token(HASH) : cond('#', PASTE, '#');
		} else if (c == '+') {
			return cond2('+', INC, '=', PLUS_EQ, '+');
		} else if (c == '-') {
			return cond3('-', DEC, '=', SUB_EQ, '>', ARROW, '-');
		} else if (c == '*') {
			return cond('=', MULT_EQ, '*');
		} else if (c == '/') {
			int d = read();
			if (d == '*') return cComment();
			if (d == '/') return cppComment();
			if (d == '=') return _marked_token(DIV_EQ);
			unread(d);
			return _marked_token('/');
		} else if (c == '%') {
			int d = read();
			if (d == '=') return _marked_token(MOD_EQ);
			if (digraphs) {
				if (d == '>') return _marked_token('}');    // digraph
				if (d == ':') {
					int e = read();
					if (e == '%') {
						int f = read();
						if (f == ':') return _marked_token(PASTE);    // digraph
						unread(f);    // Unread 2 chars here.
					}
					unread(e);
					return _marked_token('#');    // digraph
				}
			}
			unread(d);
			return _marked_token('%');
		} else if (c == ':') {
			return digraphs ? cond('>', ']', ':') : _marked_token(':');
		} else if (c == '<') {
			if (isInclude) return header(false);
			int d = read();
			if (d == '=') return _marked_token(LE);
			if (d == '<') return cond('=', LSH_EQ, LSH);
			if (digraphs) {
				if (d == ':') return _marked_token('[');    // digraph
				if (d == '%') return _marked_token('{');    // digraph
			}
			unread(d);
			return _marked_token('<');
		} else if (c == '=') {
			return cond('=', EQ, '=');
		} else if (c == '>') {
			int d = read();
			if (d == '=') return _marked_token(GE);
			if (d == '>') return cond('=', RSH_EQ, RSH);
			unread(d);
			return _marked_token('>');
		} else if (c == '^') {
			return cond('=', XOR_EQ, '^');
		} else if (c == '|') {
			int d = read();
			if (d == '=') return _marked_token(OR_EQ);
			if (d == '|') return cond('=', LOR_EQ, LOR);
			unread(d);
			return _marked_token('|');
		} else if (c == '&') {
			int d = read();
			if (d == '&') return cond('=', LAND_EQ, LAND);
			if (d == '=') return _marked_token(AND_EQ);
			unread(d);
			return _marked_token('&');
		} else if (c == '.') {
			int d = read();
			if (d == '.') return cond('.', ELLIPSIS, RANGE);
			unread(d);
			if (d >= '0' && d <= '9') {
				unread('.');
				return number();
			}
			return _marked_token('.');
		} else if (c == '\'') {
			return string("'", true);
		} else if (c == 'u') {
			int d = read();
			if (d == '\'') return string("u'", true);
			if (d == '"') return string("u\"", false);
			if (d == '8') {
				int e = read();
				if (e == '\'') string("u8'", true);
				if (e == '"') return string("u8\"", false);
				if (e == 'R') {
					int f = read();
					if (f == '"') return raw_string("u8R\"");
					unread(f);
				}
				unread(e);
			} else if (d == 'R') {
				int e = read();
				if (e == '"') return raw_string("uR\"");
				unread(e);
			}
			unread(d);
			// check for identifier is out of scope here
			return null;
		} else if (c == 'U') {
			int d = read();
			if (d == '\'') return string("U'", true);
			if (d == '"') return string("U\"", false);
			if (d == 'R') {
				int e = read();
				if (e == '"') return raw_string("UR\"");
				unread(e);
			}
			unread(d);
			// check for identifier is out of scope here
			return null;
		} else if (c == 'L') {
			int d = read();
			if (d == '\'') return string("L'", true);
			if (d == '"') return string("L\"", false);
			if (d == 'R') {
				int e = read();
				if (e == '"') return raw_string("LR\"");
				unread(e);
			}
			unread(d);
			// check for identifier is out of scope here
			return null;
		} else if (c == 'R') {
			int d = read();
			if (d == '"') return raw_string("R\"");
			unread(d);
			// check for identifier is out of scope here
			return null;
		} else if (c == '"') {
			return isInclude ? header(true) : string("\"", false);
		}
		return null;
	}

	@Nonnull
	@Override
	public Token token() throws IOException, LexerException {
		_mark();
		int c = read();

		Token token = _token(c);
		if (token == null) {
			if (c == -1) {
				close();
				return _marked_token(EOF);
			} else if (isLineSeparator(c) && ppvalid) {
				this.isStartOfLine = true;
				if (isInclude) return _marked_token(NL, "\n");
				int count = 1;
				while (true) {
					int d = read();
					if (d != -1 && isLineSeparator(d)) {
						count += 1;
					} else if (d == -1 || !(isLineSeparator(d) || Character.isWhitespace(d))) {
						unread(d);
						return _marked_token(NL, "\n".repeat(count));
					}
				}
			} else if (Character.isWhitespace(c)) {
				token = whitespace(c);
			} else if (Character.isDigit(c)) {
				unread(c);
				token = number();
			} else if (Character.isJavaIdentifierStart(c)) {
				token = identifier(c);
			} else {
				String text = TokenType.getTokenText(c);
				if (text == null) {
					if ((c >>> 16) == 0)    // Character.isBmpCodePoint() is new in 1.7
						text = Character.toString((char) c);
					else
						text = new String(Character.toChars(c));
				}
				token = _marked_token(c, text);
			}
		}

		if (isStartOfLine && token.getType() != WHITESPACE && token.getType() != C_COMMENT) {
			isStartOfLine = false;
		}

		return token;
	}

	@Override
	public void close() throws IOException {
		if (reader != null) {
			reader.close();
			reader = null;
		}
		super.close();
	}

}
