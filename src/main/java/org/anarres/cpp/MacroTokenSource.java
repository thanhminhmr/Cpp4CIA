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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.anarres.cpp.Token.CPP_COMMENT;
import static org.anarres.cpp.Token.C_COMMENT;
import static org.anarres.cpp.Token.EOF;
import static org.anarres.cpp.Token.M_ARG;
import static org.anarres.cpp.Token.M_PASTE;
import static org.anarres.cpp.Token.M_STRING;
import static org.anarres.cpp.Token.STRING;

/* This source should always be active, since we don't expand macros
 * in any inactive context. */
class MacroTokenSource extends Source {

	//private static final Logger LOG = LoggerFactory.getLogger(MacroTokenSource.class);
	private final Macro macro;
	private final Iterator<Token> tokens;    /* Pointer into the macro.  */

	private final List<Macro.Argument> args;    /* { unexpanded, expanded } */

	private Iterator<Token> arg;    /* "current expansion" */

	MacroTokenSource(@Nonnull Macro m, @Nonnull List<Macro.Argument> args) {
		this.macro = m;
		this.tokens = m.getTokens().iterator();
		this.args = args;
		this.arg = null;
	}

	@Override
	boolean isExpanding(@Nonnull Macro m) {
		/* When we are expanding an arg, 'this' macro is not
		 * being expanded, and thus we may re-expand it. */
		if (/* XXX this.arg == null && */this.macro == m)
			return true;
		return super.isExpanding(m);
	}

	/* XXX Called from Preprocessor [ugly]. */
	static void escape(@Nonnull StringBuilder buf, @Nonnull CharSequence cs) {
		for (int i = 0; i < cs.length(); i++) {
			char c = cs.charAt(i);
			switch (c) {
				case '\\':
					buf.append("\\\\");
					break;
				case '"':
					buf.append("\\\"");
					break;
				case '\n':
					buf.append("\\n");
					break;
				case '\r':
					buf.append("\\r");
					break;
				default:
					buf.append(c);
			}
		}
	}

	@Nonnull
	private Token stringify(@Nonnull Token pos, @Nonnull Macro.Argument arg) {
		StringBuilder buf = new StringBuilder();
		for (Token tok : arg) buf.append(tok.getText());
		// System.out.println("Concat: " + arg + " -> " + buf);
		StringBuilder str = new StringBuilder("\"");
		escape(str, buf);
		str.append("\"");
		// System.out.println("Escape: " + buf + " -> " + str);
		return new Token(STRING,
				pos.getLine(), pos.getColumn(),
				str.toString(), buf.toString());
	}

	/**
	 * Returns true if the given argumentIndex is the last argument of a variadic macro.
	 *
	 * @param argumentIndex The index of the argument to inspect.
	 * @return true if the given argumentIndex is the last argument of a variadic macro.
	 */
	private boolean isVariadicArgument(int argumentIndex) {
		assert argumentIndex >= 0;
		return macro.isVariadic() && argumentIndex == args.size() - 1;
	}

	/* At this point, we have consumed the first M_PASTE.
	 * @see Macro#addPaste(Token) */
	private void paste(@Nonnull Token ptok)
			throws IOException,
			LexerException {
		// List<Token> out = new ArrayList<Token>();
		StringBuilder buf = new StringBuilder();
		// Token err = null;
		/* We know here that arg is null or expired,
		 * since we cannot paste an expanded arg. */

		int count = 2;
		// While I hate auxiliary booleans, this does actually seem to be the simplest solution,
		// as it avoids duplicating all the logic around hasNext() in case COMMA.
		boolean comma = false;
		for (int i = 0; i < count; i++) {
			if (!tokens.hasNext()) {
				/* XXX This one really should throw. */
				error(ptok.getLine(), ptok.getColumn(), "Paste at end of expansion");
				buf.append(' ').append(ptok.getText());
				break;
			}
			Token tok = tokens.next();
			// System.out.println("Paste " + tok);
			if (tok.getType() == M_PASTE) {
				count += 2;
				ptok = tok;
			} else if (tok.getType() == M_ARG) {
				int idx = (Integer) tok.getValue();
				Macro.Argument arg = args.get(idx);
				if (comma && isVariadicArgument(idx) && arg.isEmpty()) {
					// Ugly way to strip the comma.
					buf.setLength(buf.length() - 1);
				} else {
					for (final Token t : arg) buf.append(t.getText());
				}
				/* XXX Test this. */
			} else if (tok.getType() == ',') {
				comma = true;
				buf.append(tok.getText());
				continue;
			} else if (tok.getType() != C_COMMENT && tok.getType() != CPP_COMMENT) {
				buf.append(tok.getText());
			}
			comma = false;
		}

		/* Push and re-lex. */
        /*
         StringBuilder		src = new StringBuilder();
         escape(src, buf);
         StringLexerSource	sl = new StringLexerSource(src.toString());
         */
		StringLexerSource sl = new StringLexerSource(buf.toString());

		/* XXX Check that concatenation produces a valid token. */
		arg = new SourceIterator(sl);
	}

	@Nonnull
	@Override
	public Token token()
			throws IOException,
			LexerException {
		for (; ; ) {
			/* Deal with lexed tokens first. */

			if (arg != null) {
				if (arg.hasNext()) {
					Token tok = arg.next();
					/* XXX PASTE -> INVALID. */
					assert tok.getType() != M_PASTE :
							"Unexpected paste token";
					return tok;
				}
				arg = null;
			}

			if (!tokens.hasNext()) return Token.eof;    /* End of macro. */
			Token tok = tokens.next();
			int idx;
			switch (tok.getType()) {
				case M_STRING:
					/* Use the nonexpanded arg. */
					idx = (Integer) tok.getValue();
					return stringify(tok, args.get(idx));
				case M_ARG:
					/* Expand the arg. */
					idx = (Integer) tok.getValue();
					// System.out.println("Pushing arg " + args.get(idx));
					arg = args.get(idx).expansion();
					break;
				case M_PASTE:
					paste(tok);
					break;
				default:
					return tok;
			}
		} /* for */

	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("expansion of ").append(macro.getName());
		Source parent = getParent();
		if (parent != null) buf.append(" in ").append(parent);
		return buf.toString();
	}
}
