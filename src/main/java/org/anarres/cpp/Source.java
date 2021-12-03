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

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;

/**
 * An input to the Preprocessor.
 * <p>
 * Inputs may come from Files, Strings or other sources. The
 * preprocessor maintains a stack of Sources. Operations such as
 * file inclusion or token pasting will push a new source onto
 * the Preprocessor stack. Sources pop from the stack when they
 * are exhausted; this may be transparent or explicit.
 * <p>
 * BUG: Error messages are not handled properly.
 */
public abstract class Source implements Closeable {
	@Nonnull static final Source INTERNAL = new Source() {
		@Nonnull
		@Override
		public Token token() throws LexerException {
			throw new LexerException("Cannot read from internal source");
		}

		@Override
		public Path getPath() {
			return null;
		}

		@Override
		public @Nonnull
		String getName() {
			return "<internal source>";
		}
	};

	private Source parent;
	private boolean autoPop;
	private PreprocessorListener listener;
	private boolean active;
	private boolean warningAsError;

	public Source() {
		this.parent = null;
		this.autoPop = false;
		this.listener = null;
		this.active = true;
		this.warningAsError = false;
	}

	/**
	 * Sets the parent source of this source.
	 * <p>
	 * Sources form a singly linked list.
	 */
	void setParent(Source parent, boolean autoPop) {
		this.parent = parent;
		this.autoPop = autoPop;
	}

	/**
	 * Returns the parent source of this source.
	 * <p>
	 * Sources form a singly linked list.
	 */
	@Nullable
	final Source getParent() {
		return parent;
	}


	void init(Preprocessor pp) {
		setListener(pp.getListener());
		this.warningAsError = pp.getWarnings().contains(Preprocessor.Warning.ERROR);
	}

	/**
	 * Sets the listener for this Source.
	 * <p>
	 * Normally this is set by the Preprocessor when a Source is
	 * used, but if you are using a Source as a standalone object,
	 * you may wish to call this.
	 */
	public void setListener(PreprocessorListener pl) {
		this.listener = pl;
	}

	/**
	 * Returns the File currently being lexed.
	 * <p>
	 * If this Source is not a {@link FileLexerSource}, then
	 * it will ask the parent Source, and so forth recursively.
	 * If no Source on the stack is a FileLexerSource, returns null.
	 */
	@Nullable
	public Path getPath() {
		Source parent = getParent();
		return parent != null ? parent.getPath() : null;
	}

	/**
	 * Returns the human-readable name of the current Source.
	 */
	@Nonnull
	public String getName() {
		Source parent = getParent();
		return parent != null ? parent.getName() : "<no file>";
	}

	/**
	 * Returns the current line number within this Source.
	 */
	public int getLine() {
		Source parent = getParent();
		return parent != null ? parent.getLine() : 0;
	}

	/**
	 * Returns the current column number within this Source.
	 */
	public int getColumn() {
		Source parent = getParent();
		return parent != null ? parent.getColumn() : 0;
	}

	/**
	 * Returns true if this Source is expanding the given macro.
	 * <p>
	 * This is used to prevent macro recursion.
	 */
	boolean isMacroExpanding(@Nonnull Macro macro) {
		Source parent = getParent();
		return parent != null && parent.isMacroExpanding(macro);
	}

	/**
	 * Returns true if this Source should be transparently popped
	 * from the input stack.
	 * <p>
	 * Examples of such sources are macro expansions.
	 */
	boolean isAutoPop() {
		return autoPop;
	}

	/**
	 * Returns true if this source has line numbers.
	 */
	boolean isNumbered() {
		return false;
	}

	/* This is an incredibly lazy way of disabling warnings when
	 * the source is not active. */
	void setActive(boolean b) {
		this.active = b;
	}

	boolean isActive() {
		return active;
	}

	/**
	 * Returns the next Token parsed from this input stream.
	 *
	 * @see Token
	 */
	@Nonnull
	public abstract Token token() throws IOException, LexerException;

	protected final void error(int line, int column, String msg) throws LexerException {
		if (listener != null) {
			listener.handleError(this, line, column, msg);
		} else {
			throw new LexerException("Error at " + line + ":" + column + ": " + msg);
		}
	}

	protected final void warning(int line, int column, String msg) throws LexerException {
		if (warningAsError) {
			error(line, column, msg);
		} else if (listener != null) {
			listener.handleWarning(this, line, column, msg);
		} else {
			throw new LexerException("Warning at " + line + ":" + column + ": " + msg);
		}
	}

	public void close() throws IOException {
	}

}
