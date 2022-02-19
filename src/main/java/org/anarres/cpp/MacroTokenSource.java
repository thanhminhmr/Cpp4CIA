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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.anarres.cpp.Token.CPP_COMMENT;
import static org.anarres.cpp.Token.C_COMMENT;
import static org.anarres.cpp.Token.IDENTIFIER;
import static org.anarres.cpp.Token.M_ARG;
import static org.anarres.cpp.Token.M_PASTE;
import static org.anarres.cpp.Token.M_STRING;

/* This source should always be active, since we don't expand macros
 * in any inactive context. */
class MacroTokenSource extends Source {

	@Nonnull private final Preprocessor preprocessor;
	@Nonnull private final Macro macro;
	@Nonnull private final List<Tokens> arguments;    /* unexpanded arguments */
	@Nonnull private final Iterator<Token> tokens;    /* Pointer into the macro.  */

	@Nonnull private final List<Tokens> expandedArgs;

	private Iterator<Token> arg;    /* "current expansion" */

	MacroTokenSource(@Nonnull Preprocessor preprocessor, @Nonnull Macro macro) {
		this.preprocessor = preprocessor;
		this.macro = macro;
		this.arguments = List.of();
		this.tokens = macro.iterator();
		this.arg = null;

		this.expandedArgs = List.of();
	}

	MacroTokenSource(@Nonnull Preprocessor preprocessor, @Nonnull Macro macro, @Nonnull List<Tokens> arguments) {
		this.preprocessor = preprocessor;
		this.macro = macro;
		this.arguments = arguments;
		this.tokens = macro.iterator();
		this.arg = null;

		this.expandedArgs = new ArrayList<>(Collections.nCopies(arguments.size(), null));
	}

	@Nonnull
	public Macro getMacro() {
		return macro;
	}

	@Override
	boolean isMacroExpanding(@Nonnull Macro macro) {
		if (this.macro == macro) return true;
		return super.isMacroExpanding(macro);
	}

	/**
	 * Returns true if the given argumentIndex is the last argument of a variadic macro.
	 *
	 * @param argumentIndex The index of the argument to inspect.
	 * @return true if the given argumentIndex is the last argument of a variadic macro.
	 */
	private boolean isVariadicArgument(int argumentIndex) {
		assert argumentIndex >= 0;
		return macro.isVariadic() && argumentIndex == arguments.size() - 1;
	}

	@Nonnull
	private Token stringify(@Nonnull Token stringifyToken) {
		final Tokens argument = arguments.get(stringifyToken.getValue(Integer.class));
		final StringBuilder builder = new StringBuilder();
		for (final Token token : argument) builder.append(token.getText());
		final String string = Token.escape(new StringBuilder("\""), builder).append('"').toString();
		return new Token(Token.STRING,
				stringifyToken.getLine(), stringifyToken.getColumn(),
				string, builder.toString());
	}

	private void paste(@Nonnull Token pasteToken) throws LexerException {
		// List<Token> out = new ArrayList<Token>();
		final StringBuilder builder = new StringBuilder();
		// Token err = null;
		/* We know here that arg is null or expired,
		 * since we cannot paste an expanded arg. */

		int count = 2;
		// While I hate auxiliary booleans, this does actually seem to be the simplest solution,
		// as it avoids duplicating all the logic around hasNext() in case COMMA.
		boolean comma = false;
		for (int i = 0; i < count; i++) {
			if (!this.tokens.hasNext()) {
				/* XXX This one really should throw. */
				error(pasteToken.getLine(), pasteToken.getColumn(), "Paste at end of expansion");
				builder.append(' ').append(pasteToken.getText());
				break;
			}
			Token tok = this.tokens.next();
			// System.out.println("Paste " + tok);
			if (tok.getType() == M_PASTE) {
				count += 2;
				pasteToken = tok;
			} else if (tok.getType() == M_ARG) {
				int idx = tok.getValue(Integer.class);
				Tokens arg = arguments.get(idx);
				if (comma && isVariadicArgument(idx) && arg.isEmpty()) {
					// Ugly way to strip the comma.
					builder.setLength(builder.length() - 1);
				} else {
					for (final Token t : arg) builder.append(t.getText());
				}
				/* XXX Test this. */
			} else if (tok.getType() == ',') {
				comma = true;
				builder.append(tok.getText());
				continue;
			} else if (tok.getType() != C_COMMENT && tok.getType() != CPP_COMMENT) {
				builder.append(tok.getText());
			}
			comma = false;
		}

		/* Push and re-lex. */
        /*
         StringBuilder		src = new StringBuilder();
         escape(src, builder);
         StringLexerSource	sl = new StringLexerSource(src.toString());
         */
		StringLexerSource sl = new StringLexerSource(builder.toString());

		/* XXX Check that concatenation produces a valid token. */
		arg = new SourceIterator(sl);
	}

	private void variadicOptional(@Nonnull Token identifierToken) {

	}

	@Nonnull
	@Override
	public Token token() throws IOException, LexerException {
		while (true) {
			// Deal with lexed tokens first.
			if (arg != null) {
				if (arg.hasNext()) return arg.next();
				this.arg = null;
			}

			// End of macro
			if (!tokens.hasNext()) return Token.eof;

			final Token token = tokens.next();
			final int type = token.getType();
			if (type == M_STRING) {
				// Use the non-expanded arg
				return stringify(token);
			} else if (type == M_ARG) {
				// Expand the arg
				final int index = token.getValue(Integer.class);
				final Tokens currentExpanded = expandedArgs.get(index);
				final Tokens expandedArg = currentExpanded != null
						? currentExpanded
						: preprocessor.macroExpandArguments(arguments.get(index));
				if (currentExpanded == null) expandedArgs.set(index, expandedArg);
				this.arg = expandedArg.iterator();
			} else if (type == M_PASTE) {
				paste(token);
			} else if (macro.isVariadic() && type == IDENTIFIER && token.getText().equals("__VA_OPT__")) {
				variadicOptional(token);
			} else {
				return token;
			}
		}
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
