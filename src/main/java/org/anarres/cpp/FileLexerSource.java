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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A {@link Source} which lexes a file.
 * <p>
 * The input is buffered.
 *
 * @see Source
 */
public class FileLexerSource extends InputLexerSource {

	@Nonnull private final Path file;

	/**
	 * Creates a new Source for lexing the given File.
	 * <p>
	 * Preprocessor directives are honoured within the file.
	 */
	public FileLexerSource(@Nonnull Path file) throws IOException {
		super(Files.newInputStream(file));
		this.file = file;
	}

	public FileLexerSource(@Nonnull Path file, @Nonnull Charset charset) throws IOException {
		super(Files.newBufferedReader(file, charset));
		this.file = file;
	}

	/**
	 * This is not necessarily the same as getFile().getPath() in case we are in a chroot.
	 */
	@Nonnull
	@Override
	public Path getPath() {
		return file;
	}

	@Nonnull
	@Override
	public String getName() {
		return file.getFileName().toString();
	}

	@Nonnull
	@Override
	public String toString() {
		return file.toString();
	}
}
