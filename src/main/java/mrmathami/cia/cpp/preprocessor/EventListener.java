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
package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;

/**
 * A handler for preprocessor events, primarily errors and warnings.
 * <p>
 * If no PreprocessorListener is installed in a Preprocessor, all
 * error and warning events will throw an exception. Installing a
 * listener allows more intelligent handling of these events.
 */
public interface EventListener {

	/**
	 * Handles a warning.
	 * A warning is triggered when something correct but misleading happened.
	 */
	void handleWarning(@Nonnull TokenSource source, int line, int column, @Nonnull String msg)
			throws PreprocessorException;

	/**
	 * Handles an error.
	 * An error is triggered when something clearly wrong happened, but could be ignored to continue lexing/parsing
	 * (keep in mind that the end result is not correct).
	 */
	void handleError(@Nonnull TokenSource source, int line, int column, @Nonnull String msg)
			throws PreprocessorException;

	/**
	 * Handles a critical error.
	 * An error is triggered when something clearly wrong happened and cannot be ignored.
	 */
	void handleCritical(@Nonnull TokenSource source, int line, int column, @Nonnull String msg)
			throws PreprocessorException;

	enum SourceChangeEvent {
		SUSPEND, PUSH, POP, RESUME
	}

	void handleSourceChange(@Nonnull TokenSource source, @Nonnull SourceChangeEvent event);

}
