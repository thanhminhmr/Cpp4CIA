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

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

class JoinReader implements Closeable {

	private final Reader in;

	private LexerSource source;
	private boolean trigraphs;
	private boolean warnings;

	private int newlines;
	private boolean flushNewLine;
	private final int[] unGetBuffer;
	private int unGetIndex;

	private boolean rawString;

	public JoinReader(Reader in, boolean trigraphs) {
		this.in = in;
		this.trigraphs = trigraphs;
		this.newlines = 0;
		this.flushNewLine = false;
		this.unGetBuffer = new int[8];
		this.unGetIndex = 0;
	}

	public JoinReader(Reader in) {
		this(in, false);
	}

	public void setTrigraphs(boolean enable, boolean warnings) {
		this.trigraphs = enable;
		this.warnings = warnings;
	}

	void init(Preprocessor pp, LexerSource s) {
		this.source = s;
		setTrigraphs(pp.getFeature(Feature.TRIGRAPHS), pp.getWarning(Warning.TRIGRAPHS));
	}

	private int __read() throws IOException {
		return unGetIndex > 0 ? unGetBuffer[--unGetIndex] : in.read();
	}

	private void _unread(int c) {
		if (c != -1) unGetBuffer[unGetIndex++] = c;
		assert unGetIndex <= unGetBuffer.length : "JoinReader ungets too many characters";
	}

	protected void warning(String msg)
			throws LexerException {
		if (source == null) throw new LexerException(msg);
		source.warning(msg);
	}

	private char trigraph(char raw, char repl) throws LexerException {
		if (trigraphs) {
			if (warnings) warning("trigraph ??" + raw + " converted to " + repl);
			return repl;
		} else {
			if (warnings) warning("trigraph ??" + raw + " ignored");
			_unread(raw);
			_unread('?');
			return '?';
		}
	}

	private int _read() throws IOException, LexerException {
		int c = __read();
		if (c == '?' && (trigraphs || warnings)) {
			int d = __read();
			if (d == '?') {
				int e = __read();
				switch (e) {
					case '(':
						return trigraph('(', '[');
					case ')':
						return trigraph(')', ']');
					case '<':
						return trigraph('<', '{');
					case '>':
						return trigraph('>', '}');
					case '=':
						return trigraph('=', '#');
					case '/':
						return trigraph('/', '\\');
					case '\'':
						return trigraph('\'', '^');
					case '!':
						return trigraph('!', '|');
					case '-':
						return trigraph('-', '~');
				}
				_unread(e);
			}
			_unread(d);
		}
		return c;
	}

	public int read() throws IOException, LexerException {
		if (flushNewLine) {
			if (newlines > 0) {
				newlines--;
				return '\n';
			}
			flushNewLine = false;
		}

		while (true) {
			int c = _read();
			if (c == '\\') {
				if (rawString) return c;
				int d = _read();
				if (d == '\n') {
					newlines++;
					continue;
				} else if (d == '\r') {
					newlines++;
					int e = _read();
					if (e != '\n')
						_unread(e);
					continue;
				}
				_unread(d);
				return c;
			} else if (c == '\r' || c == '\n' || c == '\u2028' || c == '\u2029'
					|| c == '\u000B' || c == '\u000C' || c == '\u0085') {
				flushNewLine = true;
				return c;
			} else if (c == -1) {
				if (newlines > 0) {
					newlines--;
					return '\n';
				}
				return -1;
			}
			return c;
		}
	}

	public void setRawString(boolean rawString) {
		this.rawString = rawString;
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	@Override
	public String toString() {
		return "JoinReader(nl=" + newlines + ")";
	}

}
