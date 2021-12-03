package org.anarres.cpp;

import mrmathami.annotations.Nonnull;

import java.util.ArrayList;
import java.util.List;

final class Tokens extends ArrayList<Token> {
	private static final long serialVersionUID = -1L;

	static final Tokens EMPTY = new Tokens();

	Tokens(@Nonnull Token... tokens) {
		super(List.of(tokens));
	}

	Tokens() {
	}
}
