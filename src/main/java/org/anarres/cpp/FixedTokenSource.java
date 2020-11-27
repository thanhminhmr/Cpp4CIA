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
import java.util.List;

final class FixedTokenSource extends Source {

	private final List<Token> tokens;
	private int idx = 0;

	FixedTokenSource(Token... tokens) {
		this.tokens = List.of(tokens);
	}

	FixedTokenSource(List<Token> tokens) {
		this.tokens = List.copyOf(tokens);
	}

	@Nonnull
	@Override
	public Token token() {
		return idx >= tokens.size() ? Token.eof : tokens.get(idx++);
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("constant token stream ").append(tokens);
		Source parent = getParent();
		if (parent != null) buf.append(" in ").append(parent);
		return buf.toString();
	}
}
