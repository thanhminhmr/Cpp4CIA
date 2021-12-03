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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A macro object.
 * <p>
 * This encapsulates a name, an argument count, and a token stream for replacement. The replacement token stream may
 * contain the extra tokens {@link Token#M_ARG} and {@link Token#M_STRING}.
 */
public final class Macro extends ArrayList<Token> {
	private static final long serialVersionUID = -1L;

	// standard macro
	@Nonnull static final Macro __LINE__ = new Macro(Source.INTERNAL, "__LINE__");
	@Nonnull static final Macro __FILE__ = new Macro(Source.INTERNAL, "__FILE__");
	@Nonnull static final Macro __COUNTER__ = new Macro(Source.INTERNAL, "__COUNTER__");

	@Nonnull private final Source source;
	@Nonnull private final String name;

	/* It's an explicit decision to keep these around here. We don't
	 * need to; the argument token type is M_ARG and the value
	 * is the index. The strings themselves are only used in
	 * stringification of the macro, for debugging. */
	@Nullable private final List<String> args;

	private final boolean variadic;

	public Macro(@Nonnull Source source, @Nonnull String name, @Nullable List<String> args, boolean variadic) {
		if (name.equals("defined")) {
			throw new IllegalArgumentException("Cannot redefine built-in macro 'defined'");
		}
		this.source = source;
		this.name = name;
		this.args = args;
		this.variadic = variadic;
	}

	public Macro(@Nonnull Source source, @Nonnull String name, @Nullable List<String> args) {
		this(source, name, args, false);
	}

	public Macro(@Nonnull Source source, String name) {
		this(source, name, null, false);
	}

	/**
	 * Returns the Source from which this macro was parsed.
	 * <p>
	 * This method may return null if the macro was not parsed
	 * from a regular file.
	 */
	@Nonnull
	public Source getSource() {
		return source;
	}

	/**
	 * Returns the name of this macro.
	 */
	@Nonnull
	public String getName() {
		return name;
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
	public int getNumOfArgs() {
		return args != null ? args.size() : -1;
	}

	/**
	 * Returns true if this is a variadic function-like macro.
	 */
	public boolean isVariadic() {
		return args != null && variadic;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder(name);
		if (args != null) builder.append('(').append(String.join(", ", args)).append(variadic ? "...)" : ")");
		if (!isEmpty()) {
			builder.append(" => ");
			for (final Token token : this) {
				if (token.getType() == Token.M_PASTE) {
					builder.append(token.getValue(Tokens.class).stream()
							.map(Token::getText).collect(Collectors.joining(" ## ")));
				} else {
					builder.append(token.getText());
				}
			}
		}
		return builder.toString();
	}
}
