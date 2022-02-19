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
import mrmathami.utils.EncodingDetector;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;

/**
 * A {@link Source} which lexes an {@link InputStream}.
 * <p>
 * The input is buffered.
 *
 * @see Source
 */
public class InputLexerSource extends LexerSource {

	@Nullable private final Path file;

	/**
	 * Creates a new Source for lexing the given Reader.
	 * <p>
	 * Preprocessor directives are honoured within the file.
	 */
	public InputLexerSource(@Nonnull InputStream input, @Nullable Path file) throws IOException {
		this(EncodingDetector.createReader(input), file);
	}

	public InputLexerSource(@Nonnull Reader input, @Nullable Path file) {
		super(input, true);
		this.file = file;
	}

	@Nullable
	@Override
	public Path getPath() {
		return file;
	}

	@Nonnull
	@Override
	public String getName() {
		return file != null ? file.getFileName().toString() : "<standard input>";
	}

	@Nonnull
	@Override
	public String toString() {
		return file != null ? file.toString() : "<standard input>";
	}
}
