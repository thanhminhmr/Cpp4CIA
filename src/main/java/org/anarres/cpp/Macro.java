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
import java.util.Iterator;
import java.util.List;

/**
 * A macro object.
 * <p>
 * This encapsulates a name, an argument count, and a token stream
 * for replacement. The replacement token stream may contain the
 * extra tokens {@link Token#M_ARG} and {@link Token#M_STRING}.
 */
public class Macro {

	private Source source;
	private final String name;
	/* It's an explicit decision to keep these around here. We don't
	 * need to; the argument token type is M_ARG and the value
	 * is the index. The strings themselves are only used in
	 * stringification of the macro, for debugging. */
	private List<String> args;
	private boolean variadic;
	private final List<Token> tokens;

	public Macro(Source source, String name, List<String> args) {
		this.source = source;
		this.name = name;
		this.args = args;
		this.variadic = false;
		this.tokens = new ArrayList<>();
	}

	public Macro(Source source, String name) {
		this(source, name, null);
	}

	public Macro(String name) {
		this(null, name);
	}

	/**
	 * Sets the Source from which this macro was parsed.
	 */
	public void setSource(Source s) {
		this.source = s;
	}

	/**
	 * Returns the Source from which this macro was parsed.
	 * <p>
	 * This method may return null if the macro was not parsed
	 * from a regular file.
	 */
	public Source getSource() {
		return source;
	}

	/**
	 * Returns the name of this macro.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the arguments to this macro.
	 */
	public void setArgs(List<String> args) {
		this.args = List.copyOf(args);
	}

	/**
	 * Returns true if this is a function-like macro.
	 */
	public boolean isFunctionLike() {
		return args != null;
	}

	/**
	 * Returns the number of arguments to this macro.
	 */
	public int getArgs() {
		return args.size();
	}

	/**
	 * Sets the variadic flag on this Macro.
	 */
	public void setVariadic(boolean b) {
		this.variadic = b;
	}

	/**
	 * Returns true if this is a variadic function-like macro.
	 */
	public boolean isVariadic() {
		return variadic;
	}

	/**
	 * Adds a token to the expansion of this macro.
	 */
	public void addToken(Token tok) {
		this.tokens.add(tok);
	}

	/**
	 * Adds a "paste" operator to the expansion of this macro.
	 * <p>
	 * A paste operator causes the next token added to be pasted
	 * to the previous token when the macro is expanded.
	 * It is an error for a macro to end with a paste token.
	 */
	public void addPaste(Token tok) {
		/*
		 * Given: tok0 ## tok1
		 * We generate: M_PASTE, tok0, tok1
		 * This extends as per a stack language:
		 * tok0 ## tok1 ## tok2 ->
		 *   M_PASTE, tok0, M_PASTE, tok1, tok2
		 */
		this.tokens.add(tokens.size() - 1, tok);
	}

	List<Token> getTokens() {
		return tokens;
	}

	/* Paste tokens are inserted before the first of the two pasted
	 * tokens, so it's a kind of bytecode notation. This method
	 * swaps them around again. We know that there will never be two
	 * sequential paste tokens, so a boolean is sufficient. */
	public String getText() {
		StringBuilder buf = new StringBuilder();
		boolean paste = false;
		for (Token tok : tokens) {
			if (tok.getType() == Token.M_PASTE) {
				assert !paste : "Two sequential pastes.";
				paste = true;
				continue;
			} else {
				buf.append(tok.getText());
			}
			if (paste) {
				buf.append(" ## ");
				paste = false;
			}
			// buf.append(tokens.get(i));
		}
		return buf.toString();
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder(name);
		if (args != null) {
			buf.append('(');
			Iterator<String> it = args.iterator();
			while (it.hasNext()) {
				buf.append(it.next());
				if (it.hasNext())
					buf.append(", ");
				else if (isVariadic())
					buf.append("...");
			}
			buf.append(')');
		}
		if (!tokens.isEmpty()) {
			buf.append(" => ").append(getText());
		}
		return buf.toString();
	}

	/**
	 * A macro argument.
	 * <p>
	 * This encapsulates a raw and preprocessed token stream.
	 */
	static final class Argument extends ArrayList<Token> {
		private static final long serialVersionUID = 2579190568088152392L;
		private List<Token> expansion;

		public Argument() {
			this.expansion = null;
		}

		public void addToken(@Nonnull Token tok) {
			add(tok);
		}

		void expand(@Nonnull Preprocessor p)
				throws IOException,
				LexerException {
			/* Cache expansion. */
			if (expansion == null) {
				this.expansion = p.expand(this);
				// System.out.println("Expanded arg " + this);
			}
		}

		@Nonnull
		public Iterator<Token> expansion() {
			return expansion.iterator();
		}

		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder();
			buf.append("Argument(");
			// buf.append(super.toString());
			buf.append("raw=[ ");
			for (Token value : this) buf.append(value.getText());
			buf.append(" ];expansion=[ ");
			if (expansion == null)
				buf.append("null");
			else
				for (Token token : expansion)
					buf.append(token.getText());
			buf.append(" ])");
			return buf.toString();
		}
	}
}
