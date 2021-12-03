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
import org.anarres.cpp.PreprocessorListener.SourceChangeEvent;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.anarres.cpp.Token.*;

/**
 * A C Preprocessor.
 * The Preprocessor outputs a token stream which does not need
 * re-lexing for C or C++. Alternatively, the output text may be
 * reconstructed by concatenating the {@link Token#getText() text}
 * values of the returned {@link Token Tokens}.
 */
/*
 * Source file name and line number information is conveyed by lines of the form
 *
 * # linenum filename flags
 *
 * These are called linemarkers. They are inserted as needed into
 * the output (but never within a string or character constant). They
 * mean that the following line originated in file filename at line
 * linenum. filename will never contain any non-printing characters;
 * they are replaced with octal escape sequences.
 *
 * After the file name comes zero or more flags, which are `1', `2',
 * `3', or `4'. If there are multiple flags, spaces separate them. Here
 * is what the flags mean:
 *
 * `1'
 * This indicates the start of a new file.
 * `2'
 * This indicates returning to a file (after having included another
 * file).
 * `3'
 * This indicates that the following text comes from a system header
 * file, so certain warnings should be suppressed.
 * `4'
 * This indicates that the following text should be treated as being
 * wrapped in an implicit extern "C" block.
 */
public final class Preprocessor implements Closeable {
	@Nonnull private static final Map<String, Macro> DEFAULT_MACRO = Map.ofEntries(
			Map.entry("__LINE__", Macro.__LINE__),
			Map.entry("__FILE__", Macro.__FILE__),
			Map.entry("__COUNTER__", Macro.__COUNTER__),
			// gcc specific
			Map.entry("__attribute__", new Macro(Source.INTERNAL, "__attribute__", List.of(""), true)),
			// msvc specific
			Map.entry("__cdecl", new Macro(Source.INTERNAL, "__cdecl")),
			Map.entry("__clrcall", new Macro(Source.INTERNAL, "__clrcall")),
			Map.entry("__stdcall", new Macro(Source.INTERNAL, "__stdcall")),
			Map.entry("__fastcall", new Macro(Source.INTERNAL, "__fastcall")),
			Map.entry("__thiscall", new Macro(Source.INTERNAL, "__thiscall")),
			Map.entry("__vectorcall", new Macro(Source.INTERNAL, "__vectorcall")),
			Map.entry("__declspec", new Macro(Source.INTERNAL, "__declspec", List.of(""), true))
	);

	private final Queue<Source> inputs = new LinkedList<>();
	private final Map<String, Macro> globalMacros = new HashMap<>(DEFAULT_MACRO);
	private final Map<String, Macro> localMacros = new HashMap<>();

	private final Stack<State> states = new Stack<>();
	private Source source = null;

	/* Miscellaneous support. */
	private int counter = 0;
	@Nonnull private final Set<Path> pragmaOnceFiles = new HashSet<>();
	@Nonnull private final Set<Path> existFiles = new HashSet<>();
	@Nonnull private final Set<Path> notExists = new HashSet<>();

	/* Support junk to make it work like cpp */
	@Nonnull private List<Path> quoteIncludePath = List.of(); /* -iquote */
	@Nonnull private List<Path> systemIncludePaths = List.of(); /* -I */

	private final Set<Feature> features = EnumSet.noneOf(Feature.class);
	private final Set<Warning> warnings = EnumSet.noneOf(Warning.class);
	@Nonnull private final PreprocessorListener listener;

	public Preprocessor(@Nonnull PreprocessorListener listener) {
		this.listener = listener;
		states.push(new State());
	}


	/**
	 * Returns the PreprocessorListener which handles events for
	 * this Preprocessor.
	 */
	@Nonnull
	public PreprocessorListener getListener() {
		return listener;
	}

	/**
	 * Returns the feature-set for this Preprocessor.
	 * <p>
	 * This set may be freely modified by user code.
	 */
	@Nonnull
	public Set<Feature> getFeatures() {
		return features;
	}

	/**
	 * Adds a feature to the feature-set of this Preprocessor.
	 */
	public void addFeature(@Nonnull Feature feature) {
		features.add(feature);
	}

	/**
	 * Adds features to the feature-set of this Preprocessor.
	 */
	public void addFeatures(@Nonnull Collection<Feature> features) {
		this.features.addAll(features);
	}

	/**
	 * Returns true if the given feature is in
	 * the feature-set of this Preprocessor.
	 */
	public boolean getFeature(@Nonnull Feature feature) {
		return features.contains(feature);
	}

	/**
	 * Returns the warning-set for this Preprocessor.
	 * <p>
	 * This set may be freely modified by user code.
	 */
	@Nonnull
	public Set<Warning> getWarnings() {
		return warnings;
	}

	/**
	 * Adds a warning to the warning-set of this Preprocessor.
	 */
	public void addWarning(@Nonnull Warning warning) {
		warnings.add(warning);
	}

	/**
	 * Adds warnings to the warning-set of this Preprocessor.
	 */
	public void addWarnings(@Nonnull Collection<Warning> warnings) {
		this.warnings.addAll(warnings);
	}

	/**
	 * Returns true if the given warning is in
	 * the warning-set of this Preprocessor.
	 */
	public boolean getWarning(@Nonnull Warning warning) {
		return warnings.contains(warning);
	}

	/**
	 * Adds input for the Preprocessor.
	 * <p>
	 * Inputs are processed in the order in which they are added.
	 */
	public void addInput(@Nonnull Source source) {
		// TODO: change this
		source.init(this);
		inputs.add(source);
	}

	/**
	 * Handles an error.
	 * <p>
	 * If a PreprocessorListener is installed, it receives the error. Otherwise, an exception is thrown.
	 */
	private void error(int line, int column, @Nonnull String msg) throws LexerException {
		listener.handleError(source, line, column, msg);
	}

	/**
	 * Handles an error.
	 * <p>
	 * If a PreprocessorListener is installed, it receives the error. Otherwise, an exception is thrown.
	 *
	 * @see #error(int, int, String)
	 */
	private void error(@Nonnull Token tok, @Nonnull String msg) throws LexerException {
		error(tok.getLine(), tok.getColumn(), msg);
	}

	/**
	 * Handles a warning.
	 * <p>
	 * If a PreprocessorListener is installed, it receives the warning. Otherwise, an exception is thrown.
	 */
	private void warning(int line, int column, @Nonnull String msg) throws LexerException {
		if (warnings.contains(Warning.ERROR)) {
			error(line, column, msg);
		} else {
			listener.handleWarning(source, line, column, msg);
		}
	}

	/**
	 * Handles a warning.
	 * <p>
	 * If a PreprocessorListener is installed, it receives the warning. Otherwise, an exception is thrown.
	 *
	 * @see #warning(int, int, String)
	 */
	private void warning(@Nonnull Token tok, @Nonnull String msg) throws LexerException {
		warning(tok.getLine(), tok.getColumn(), msg);
	}


	/**
	 * Sets the user include paths used by this Preprocessor.
	 */
	public void setQuoteIncludePaths(@Nonnull List<Path> paths) {
		this.quoteIncludePath = List.copyOf(paths);
	}

	/**
	 * Returns the user include-path of this Preprocessor.
	 * <p>
	 * This list may be freely modified by user code.
	 */
	@Nonnull
	public List<Path> getQuoteIncludePaths() {
		return Collections.unmodifiableList(quoteIncludePath);
	}

	/**
	 * Sets the system include paths used by this Preprocessor.
	 */
	public void setSystemIncludePath(@Nonnull List<Path> paths) {
		this.systemIncludePaths = List.copyOf(paths);
	}

	/**
	 * Returns the system include-path of this Preprocessor.
	 * <p>
	 * This list may be freely modified by user code.
	 */
	@Nonnull
	public List<Path> getSystemIncludePaths() {
		return Collections.unmodifiableList(systemIncludePaths);
	}

	/**
	 * Adds a Macro to this Preprocessor.
	 * <p>
	 * The given {@link Macro} object encapsulates both the name
	 * and the expansion.
	 */
	public void addGlobalMacro(@Nonnull Macro macro) {
		globalMacros.put(macro.getName(), macro);
	}

	/**
	 * Returns the Map of Macros parsed during the run of this
	 * Preprocessor.
	 *
	 * @return The {@link Map} of macros currently defined.
	 */
	@Nonnull
	public Map<String, Macro> getGlobalMacros() {
		return Collections.unmodifiableMap(globalMacros);
	}

	/**
	 * Returns the named macro.
	 * <p>
	 * While you can modify the returned object, unexpected things
	 * might happen if you do.
	 *
	 * @return the Macro object, or null if not found.
	 */
	@Nullable
	public Macro getMacro(@Nonnull String name) {
		return globalMacros.get(name);
	}


	/* States (for #if #elif #else #endif) */
	private void statePush() {
		final State state = states.peek();
		states.push(new State(state));
	}

	private void statePop() throws LexerException {
		final State state = states.pop();
		if (states.isEmpty()) {
			error(0, 0, "#endif without #if");
			states.push(state);
		}
	}

	private boolean stateIsActive() {
		final State state = states.peek();
		return state.isParentActive() && state.isActive();
	}


	/* Sources */

	/* XXX Make this include the NL, and make all cpp directives eat
	 * their own NL. */
	@Nonnull
	private Token createLineToken(int line, @Nonnull String name, @Nonnull String extra) {
		return new Token(P_LINE, line, 0,
				Token.escape(new StringBuilder("#line ").append(line).append(" \""), name)
						.append('"').append(extra).append('\n').toString());
	}

	/**
	 * Get the next source from the input queue
	 */
	@Nonnull
	private Token nextInputSource() {
		final Source source = inputs.poll();
		if (source == null) return Token.eof;
		// TODO: reset local macro
		sourcePush(source, true);
		return createLineToken(source.getLine(), source.getName(), " 1");
	}


	/**
	 * Pushes a Source onto the input stack.
	 *
	 * @param source  the new Source to push onto the top of the input stack.
	 * @param autoPop if true, the Source is automatically removed from the input stack at EOF.
	 * @see #sourcePop()
	 */
	private void sourcePush(@Nonnull Source source, boolean autoPop) {
		source.init(this);
		source.setParent(this.source, autoPop);
		if (this.source != null) listener.handleSourceChange(this.source, SourceChangeEvent.SUSPEND);
		this.source = source;
		listener.handleSourceChange(this.source, SourceChangeEvent.PUSH);
	}

	/**
	 * Pops a Source from the input stack.
	 *
	 * @throws IOException if an I/O error occurs.
	 * @see #sourcePush(Source, boolean)
	 */
	@Nullable
	private Token sourcePop() throws IOException {
		listener.handleSourceChange(this.source, SourceChangeEvent.POP);
		Source oldSource = this.source;
		this.source = oldSource.getParent();
		/* Always a noop unless called externally. */
		oldSource.close();
		if (source == null) return nextInputSource();
		listener.handleSourceChange(source, SourceChangeEvent.RESUME);
		if (getFeature(Feature.LINEMARKERS) && oldSource.isNumbered()) {
			/* We actually want 'did the nested source
			 * contain a newline token', which isNumbered()
			 * approximates. This is not perfect, but works. */
			return createLineToken(source.getLine(), source.getName(), " 2");
		}
		return null;
	}

	/* Source tokens */
	@Nonnull private final Stack<Token> sourcePushbackTokens = new Stack<>();

	@Nonnull
	private Token sourceGetTokenCheckNextAndPushback() throws IOException, LexerException {
		if (sourcePushbackTokens.isEmpty()) {
			if (source == null) {
				final Token token = nextInputSource();
				if (token.getType() != P_LINE || getFeature(Feature.LINEMARKERS)) {
					return token;
				}
			}
			return source != null ? source.token() : Token.eof;
		} else {
			return sourcePushbackTokens.pop();
		}
	}

	@Nonnull
	private Token sourceGetToken() throws IOException, LexerException {
		while (true) {
			final Token token = sourceGetTokenCheckNextAndPushback();
			if (token.getType() == EOF && source != null && source.isAutoPop()) {
				final Token lineMark = sourcePop();
				if (lineMark != null) return lineMark;
				continue;
			}
			return token;
		}
	}

	private void sourceUndoGetToken(@Nonnull Token token) {
		sourcePushbackTokens.push(token);
	}

	@Nonnull
	private Token sourceGetTokenSkipWS() throws IOException, LexerException {
		while (true) {
			final Token token = sourceGetToken();
			final int type = token.getType();
			if (type != WHITESPACE && type != C_COMMENT && type != CPP_COMMENT) return token;
		}
	}

	/**
	 * Returns an NL or an EOF token.
	 * <p>
	 * The metadata on the token will be correct, which is better
	 * than generating a new one.
	 * <p>
	 * This method can, as of recent patches, return a P_LINE token.
	 */
	@Nonnull
	private Token sourceSkipLine(boolean whitespaceExpected) throws IOException, LexerException {
		while (true) {
			final Token token = sourceGetTokenCheckNextAndPushback();
			final int type = token.getType();
			if (type == EOF) {
				warning(token, "No newline before end of file");
				// insert a virtual new line here
				return new Token(NEW_LINE, token.getLine(), token.getColumn(), "\n");
			} else if (type == NEW_LINE) {
				return token;
			} else if (whitespaceExpected && type != C_COMMENT && type != CPP_COMMENT && type != WHITESPACE) {
				warning(token, "Unexpected nonwhite token");
			}
		}
	}


	private boolean lookAheadForOpenBraceAndConsumeSkipWS() throws IOException, LexerException {
		// looking for the open brace token
		// keep all lookAhead WS/comment tokens
		// if found open brace, consume it
		// if not, un-get all look-ahead tokens
		final Stack<Token> lookAhead = new Stack<>();
		while (true) {
			final Token token = sourceGetToken();
			final int type = token.getType();
			if (type == '(') {
				break;
			} else if (type == WHITESPACE || type == C_COMMENT || type == CPP_COMMENT || type == NEW_LINE) {
				lookAhead.push(token);
			} else {
				sourceUndoGetToken(token);
				while (!lookAhead.isEmpty()) {
					sourceUndoGetToken(lookAhead.pop());
				}
				return false;
			}
		}
		return true;
	}

	/* parse and expand a macro in use */
	boolean parseMacroInUse(@Nonnull Macro macro, @Nonnull Token macroName)
			throws IOException, LexerException {
		if (macro == Macro.__LINE__) {
			final String string = Integer.toString(macroName.getLine());
			sourcePush(new FixedTokenSource(
					new Token(NUMBER, macroName.getLine(), macroName.getColumn(),
							string, new NumberToken(10, string))), true);
		} else if (macro == Macro.__FILE__) {
			final String sourceName = source.getName();
			final String string = Token.escape(new StringBuilder("\""), sourceName).append('"').toString();
			sourcePush(new FixedTokenSource(
					new Token(STRING, macroName.getLine(), macroName.getColumn(),
							string, sourceName)), true);
		} else if (macro == Macro.__COUNTER__) {
			final int value = this.counter++;
			final String string = Integer.toString(value);
			sourcePush(new FixedTokenSource(
					new Token(NUMBER, macroName.getLine(), macroName.getColumn(),
							string, new NumberToken(10, string))), true);
		} else if (macro.isFunctionLike()) {
			if (!lookAheadForOpenBraceAndConsumeSkipWS()) return false;
			final boolean isMacroVariadic = macro.isVariadic(); // short-hand
			final int macroNumOfArgs = macro.getNumOfArgs(); // short-hand

			final List<Tokens> arguments = new ArrayList<>(macroNumOfArgs);
			int depth = 0;
			boolean spaced = false;
			Tokens argument = null;
			while (true) {
				// parse arguments...
				final int currentSize = arguments.size();
				final Token token = sourceGetTokenSkipWS();
				final int type = token.getType();
				if (type == EOF) {
					error(token, "EOF in macro args");
					return false;
				} else if (type == WHITESPACE || type == C_COMMENT || type == CPP_COMMENT || type == NEW_LINE) {
					spaced = true;
				} else if (type == ',' && depth == 0 && (!isMacroVariadic || currentSize < macroNumOfArgs)) {
					arguments.add(argument != null ? argument : Tokens.EMPTY);
					argument = null;
					spaced = false;
				} else if (type == ')' && depth == 0) {
					// check if argument are valid
					if (argument != null) arguments.add(argument);
					// check and add empty VA_ARGS if needed
					if (isMacroVariadic && currentSize == macroNumOfArgs - 1) arguments.add(Tokens.EMPTY);
					// check argument size
					if (currentSize == macroNumOfArgs) {
						sourcePush(new MacroTokenSource(this, macro, arguments), true);
						return true;
					}
					// failed
					error(macroName, "Macro \"" + macro.getName() + "\" passed " + currentSize
							+ " arguments(s) but needs " + (isMacroVariadic ? macroNumOfArgs - 1 : macroNumOfArgs)
							+ (isMacroVariadic ? " or more arguments" : "arguments"));
					return false;
				} else {
					depth += type == '(' ? 1 : type == ')' ? -1 : 0;
					argument = argument != null ? argument : new Tokens();
					if (spaced && argument.isEmpty()) argument.add(Token.whitespace);
					argument.add(token);
				}
			}
		} else {
			sourcePush(new MacroTokenSource(this, macro), true);
		}
		return true;
	}

	/**
	 * Expands an argument.
	 */
	/* I'd rather this were done lazily, but doing so breaks spec. */
	@Nonnull
	Tokens macroExpandArguments(@Nonnull Tokens arg) throws IOException, LexerException {
		Tokens expansion = new Tokens();
		boolean space = false;

		sourcePush(new FixedTokenSource(arg), false);

		while (true) {
			final Token tok = expanded_token();
			final int type = tok.getType();
			if (type == EOF) {
				break;
			} else if (type == WHITESPACE || type == C_COMMENT || type == CPP_COMMENT) {
				space = true;
			} else {
				if (space && !expansion.isEmpty()) expansion.add(Token.whitespace);
				expansion.add(tok);
				space = false;
			}
		}

		// Always returns null.
		sourcePop();

		return expansion;
	}

	/**
	 * Return null if parse argument names success.
	 * Return a token when parse argument when wrong.
	 */
	@Nullable
	private Token parseMacroArgumentNames(@Nonnull List<String> argumentNames) throws IOException, LexerException {
		{
			final Token openToken = sourceGetToken();
			if (openToken.getType() != '(') {
				// not an function-like macro
				sourceUndoGetToken(openToken);
				return null;
			}
		}
		{
			final Token closeToken = sourceGetTokenSkipWS();
			if (closeToken.getType() == ')') {
				// function-like macro without parameters
				argumentNames.add(null);
				return null;
			} else {
				sourceUndoGetToken(closeToken);
			}
		}

		while (true) {
			{
				final Token identifierToken = sourceGetTokenSkipWS();
				switch (identifierToken.getType()) {
					case IDENTIFIER:
						argumentNames.add(identifierToken.getText());
						break;
					case ELLIPSIS:
						// Unnamed Variadic macro
						argumentNames.add("__VA_ARGS__");
						// We just named the ellipsis, but we unget the token
						// to allow the ELLIPSIS handling below to process it.
						sourceUndoGetToken(identifierToken);
						break;
					case NEW_LINE:
					case EOF:
						error(identifierToken, "Unterminated macro parameter list");
						return identifierToken;
					default:
						error(identifierToken, "error in macro parameters: " + identifierToken.getText());
						return sourceSkipLine(false);
				}
			}
			{
				final Token separateToken = sourceGetTokenSkipWS();
				switch (separateToken.getType()) {
					case ',':
						continue;
					case ELLIPSIS: {
						// Variadic macro, add null to the end to mark that
						argumentNames.add(null);
						final Token closeToken = sourceGetTokenSkipWS();
						final int type = closeToken.getType();
						if (type != ')') {
							error(separateToken, "Ellipsis must be on last argument");
							return type == NEW_LINE || type == EOF ? closeToken : sourceSkipLine(false);
						}
					}
					case ')':
						// skip all whitespace
						sourceUndoGetToken(sourceGetTokenSkipWS());
						return null;

					case NEW_LINE:
					case EOF:
						error(separateToken, "Unterminated macro parameters");
						return separateToken;
					default:
						error(separateToken, "Bad token in macro parameters: " + separateToken.getText());
						return sourceSkipLine(false);
				}
			}
		}
	}

	@Nullable
	private Token parseMacroTokens(@Nonnull Macro macro, @Nullable List<String> argumentNames)
			throws IOException, LexerException {
		boolean spaced = false;
		Tokens pasted = null;
		while (true) {
			final Token token;
			final Token macroToken = sourceGetToken();
			final int type = macroToken.getType();
			switch (type) {
				case C_COMMENT:
				case CPP_COMMENT:
				case WHITESPACE:
					// GNU's cpp -CC keep the comment (and convert line to block comment)
					// then expand it with the macro.
					// Instead we just throw all comment out.
					spaced = true;
					continue;

				case P_PASTE: {
					spaced = false;
					// if pasted != null then multiple consecutive paste are met. Simply skip them.
					if (pasted != null) continue;
					final int index = macro.size() - 1;
					if (index < 0) {
						// check empty macro
						error(macroToken, "Macro cannot start with paste token: " + macroToken.getText());
						return sourceSkipLine(false);
					}
					// paste with previous token
					final Token previousToken = macro.get(index);
					if (previousToken.getType() != M_PASTE) {
						macro.set(index, new Token(M_PASTE, previousToken.getLine(), previousToken.getColumn(),
								"##", pasted = new Tokens(previousToken)));
					} else {
						pasted = previousToken.getValue(Tokens.class);
					}
					continue;
				}

				case EOF:
				case NEW_LINE:
					// finish parsing
					if (pasted != null) {
						error(macroToken, "Macro cannot end with paste token: " + macroToken.getText());
						return macroToken;
					}
					// undo the end of line
					sourceUndoGetToken(macroToken);
					return null;

				case '#': {
					final Token nextToken = sourceGetTokenSkipWS();
					final int tokenType = nextToken.getType();
					if (tokenType == IDENTIFIER && argumentNames != null) {
						final int index = argumentNames.indexOf(nextToken.getText());
						if (index >= 0) {
							token = new Token(M_STRING,
									nextToken.getLine(), nextToken.getColumn(),
									"#" + nextToken.getText(), index);
							break;
						}
					}
					error(nextToken, "Stringify must be followed by a macro parameter: " + nextToken.getText());
					return tokenType == NEW_LINE || tokenType == EOF ? nextToken : sourceSkipLine(false);
				}
				case IDENTIFIER: {
					final int index = argumentNames != null
							? argumentNames.indexOf(macroToken.getText())
							: -1;
					token = index >= 0 ? new Token(M_ARG,
							macroToken.getLine(), macroToken.getColumn(),
							macroToken.getText(), index) : macroToken;
					break;
				}
				default:
					token = macroToken;
			}
			if (pasted != null) {
				pasted.add(token);
				pasted = null;
			} else {
				if (spaced && !macro.isEmpty()) macro.add(Token.whitespace);
				macro.add(token);
				spaced = false;
			}
		}
	}

	/* processes a #define directive */
	@Nonnull
	private Token parseDefineDirective() throws IOException, LexerException {
		/* parse macro name */

		final Token identifierToken = sourceGetTokenSkipWS();
		{
			final int tokenType = identifierToken.getType();
			if (tokenType != IDENTIFIER) {
				error(identifierToken, "Expected identifier instead of " + identifierToken);
				return tokenType == NEW_LINE || tokenType == EOF ? identifierToken : sourceSkipLine(false);
			}
		}
		final String macroName = identifierToken.getText();
		if ("defined".equals(macroName)) {
			error(identifierToken, "Cannot redefine name 'defined'");
			return sourceSkipLine(false);
		}

		/* parse macro argument names */
		final boolean macroVariadic;
		final List<String> macroArgNames;
		{
			final List<String> parseArgNames = new ArrayList<>();
			final Token token = parseMacroArgumentNames(parseArgNames);
			if (token != null) return token;
			if (parseArgNames.isEmpty()) {
				macroVariadic = false;
				macroArgNames = null;
			} else {
				macroVariadic = parseArgNames.remove(null) && !parseArgNames.isEmpty();
				macroArgNames = parseArgNames;
			}
		}

		/* parse macro tokens */
		final Macro macro = new Macro(source, macroName, macroArgNames, macroVariadic);
		{
			final Token token = parseMacroTokens(macro, macroArgNames);
			if (token != null) return token;
		}
		addGlobalMacro(macro);

		return sourceGetToken(); /* NL or EOF. */
	}

	@Nonnull
	private Token parseUndefDirective() throws IOException, LexerException {
		final Token token = sourceGetTokenSkipWS();
		if (token.getType() != IDENTIFIER) {
			error(token, "Expected identifier, not " + token);
			if (token.getType() == NEW_LINE || token.getType() == EOF) return token;
		} else {
			final Macro macro = getMacro(token.getText());
			if (macro != null) {
				/* XXX error if predefined */
				globalMacros.remove(macro.getName());
			}
		}
		return sourceSkipLine(true);
	}

	/**
	 * Attempts to include the given file.
	 *
	 * @param file The Path to file to attempt to include.
	 * @return true if the file was successfully included, false otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	private boolean include(@Nonnull Path file) throws IOException {
		final Path realFile = file.normalize().toAbsolutePath();
		if (notExists.contains(realFile)) return false;
		if (existFiles.contains(realFile)) {
			if (!pragmaOnceFiles.contains(realFile)) {
				sourcePush(new FileLexerSource(realFile), true);
			}
			return true;
		} else if (Files.isRegularFile(realFile)) {
			existFiles.add(realFile);
			sourcePush(new FileLexerSource(realFile), true);
			return true;
		} else {
			notExists.add(realFile);
		}
		return false;
	}

	private boolean isPathExists(@Nonnull Path file) {
		final Path realFile = file.normalize().toAbsolutePath();
		if (notExists.contains(realFile)) return false;
		if (existFiles.contains(realFile)) return true;
		if (Files.isRegularFile(realFile)) {
			existFiles.add(realFile);
			return true;
		} else {
			notExists.add(realFile);
			return false;
		}
	}

	/**
	 * Attempts to include a file from an include paths, by name.
	 *
	 * @param includePaths The list of virtual directories to search for the given name.
	 * @param name         The name of the file to attempt to include.
	 * @return true if the file was successfully included, false otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	private boolean include(@Nonnull List<Path> includePaths, @Nonnull String name, boolean next) throws IOException {
		// if include_next then we have to find the include path first
		boolean nextScan = next;
		for (final Path path : includePaths) {
			final Path currentFile = path.resolve(name);
			if (nextScan) {

				if (isPathExists(currentFile)) nextScan = false;
				continue;
			}

			if (include(currentFile)) return true;
		}
		return false;
	}

	/**
	 * Handles an include directive.
	 *
	 * @throws IOException    if an I/O error occurs.
	 * @throws LexerException if the include fails, and the error handler is fatal.
	 */
	private void include(@Nullable Path parent, int line, @Nonnull String name, boolean quoted, boolean next)
			throws IOException, LexerException {
		// if path is absolute, skip it. We don't have file system model here
		if (Path.of(name).isAbsolute()
				|| quoted && parent != null && include(parent.resolveSibling(name))
				|| (next || !quoted) && include(systemIncludePaths, name, next)) {
			error(line, 0, "Include file not found: " + (quoted ? '\"' : '<') + name + (quoted ? '\"' : '>'));
		}
	}

	@Nonnull
	private Token parseIncludeDirective(boolean next) throws IOException, LexerException {
		assert source instanceof LexerSource : "Cannot include from non-lex source!";
		final LexerSource lexer = (LexerSource) source;
		try {
			lexer.setInclude(true);
			final Token token = token_nonwhite();
			final int type = token.getType();

			if (type != HEADER) {
				error(token, "Expected header name, found " + token.getText());
				return type != NEW_LINE && type != EOF ? sourceSkipLine(false) : token;
			}

			final String name = token.getValue(String.class);
			final Token newlineToken = sourceSkipLine(true);

			/* Do the inclusion. */
			include(source.getPath(), token.getLine(), name, token.getText().startsWith("\""), next);

			return getFeature(Feature.LINEMARKERS) ? createLineToken(1, source.getName(), " 1") : newlineToken;
		} finally {
			lexer.setInclude(false);
		}
	}

	private void pragma_once() throws IOException {
		if (!pragmaOnceFiles.add(source.getPath())) {
			final Token lineMarker = sourcePop();
			// FixedTokenSource should never generate a line-marker on exit.
			if (lineMarker != null) sourcePush(new FixedTokenSource(lineMarker), true);
		}
	}

	private void pragma(@Nonnull Token name, @Nonnull List<Token> value) throws IOException, LexerException {
		if (getFeature(Feature.PRAGMA_ONCE)) {
			if ("once".equals(name.getText())) {
				pragma_once();
				return;
			}
		}
		warning(name, "Unknown #pragma: " + name.getText());
	}

	@Nonnull
	private Token parsePragmaDirective() throws IOException, LexerException {
		final Token name = sourceGetTokenSkipWS();
		{
			final int type = name.getType();
			if (type == EOF) {
				warning(name, "End of file in #pragma");
				return name;
			} else if (type == NEW_LINE) {
				warning(name, "Empty #pragma");
				return name;
			} else if (type != IDENTIFIER) {
				warning(name, "Illegal #pragma " + name.getText());
				return sourceSkipLine(false);
			}
		}
		final List<Token> value = new ArrayList<>();
		boolean whitespace = false;
		while (true) {
			final Token token = sourceGetToken(); // cannot skip whitespace here, might be needed
			final int type = token.getType();
			if (type == C_COMMENT || type == CPP_COMMENT || type == WHITESPACE) {
				whitespace = true;
			} else if (type != EOF && type != NEW_LINE) {
				if (whitespace) value.add(Token.whitespace);
				value.add(token);
			} else {
				if (type == EOF) warning(token, "End of file in #pragma");
				pragma(name, value);
				return token;
			}
		}
	}

	/* For #error and #warning. */
	private void parseErrorDirective(@Nonnull Token directiveName) throws IOException, LexerException {
		final String name = directiveName.getText();
		final StringBuilder builder = new StringBuilder();
		builder.append('#').append(name).append(' ');
		/* Peculiar construction to ditch first whitespace. */
		Token tok = sourceGetTokenSkipWS();
		while (true) {
			final int type = tok.getType();
			if (type == NEW_LINE || type == EOF) {
				break;
			} else {
				builder.append(tok.getText());
			}
			tok = sourceGetToken();
		}
		if (name.equals("error")) {
			error(directiveName, builder.toString());
		} else {
			warning(directiveName, builder.toString());
		}
	}

	/* This bypasses token() for #elif expressions.
	 * If we don't do this, then isActive() == false
	 * causes token() to simply chew the entire input line. */
	@Nonnull
	private Token expanded_token() throws IOException, LexerException {
		while (true) {
			final Token token = sourceGetToken();
			if (token.getType() == IDENTIFIER) {
				final Macro macro = getMacro(token.getText());
				if (macro == null || source.isMacroExpanding(macro)) return token;
				if (parseMacroInUse(macro, token)) continue;
			}
			return token;
		}
	}

	@Nonnull
	private Token expanded_token_nonwhite() throws IOException, LexerException {
		while (true) {
			final Token token = expanded_token();
			final int type = token.getType();
			if (type != WHITESPACE && type != C_COMMENT && type != CPP_COMMENT) return token;
		}
	}

	@Nullable
	private Token expr_token = null;

	@Nonnull
	private Token expr_token() throws IOException, LexerException {
		Token tok = expr_token;

		if (tok != null) {
			// System.out.println("ungetting");
			expr_token = null;
		} else {
			tok = expanded_token_nonwhite();
			// System.out.println("expt is " + tok);

			if (tok.getType() == IDENTIFIER && tok.getText().equals("defined")) {
				Token la = sourceGetTokenSkipWS();
				boolean paren = false;
				if (la.getType() == '(') {
					paren = true;
					la = sourceGetTokenSkipWS();
				}

				// System.out.println("Core token is " + la);
				if (la.getType() != IDENTIFIER) {
					error(la, "defined() needs identifier, not " + la.getText());
					tok = new Token(NUMBER, la.getLine(), la.getColumn(), "0", new NumberToken(10, "0"));
				} else if (globalMacros.containsKey(la.getText())) {
					// System.out.println("Found macro");
					tok = new Token(NUMBER, la.getLine(), la.getColumn(), "1", new NumberToken(10, "1"));
				} else {
					// System.out.println("Not found macro");
					tok = new Token(NUMBER, la.getLine(), la.getColumn(), "0", new NumberToken(10, "0"));
				}

				if (paren) {
					la = sourceGetTokenSkipWS();
					if (la.getType() != ')') {
						expr_untoken(la);
						error(la, "Missing ) in defined(). Got " + la.getText());
					}
				}
			}
		}

		// System.out.println("expr_token returns " + tok);
		return tok;
	}

	private void expr_untoken(@Nonnull Token tok) {
		assert expr_token == null : "Cannot unget two expression tokens.";
		expr_token = tok;
	}

	private int expr_priority(@Nonnull Token op) {
		switch (op.getType()) {
			case '/':
				return 11;
			case '%':
				return 11;
			case '*':
				return 11;
			case '+':
				return 10;
			case '-':
				return 10;
			case LEFT_SHIFT:
				return 9;
			case RIGHT_SHIFT:
				return 9;
			case '<':
				return 8;
			case '>':
				return 8;
			case LESS_EQUAL:
				return 8;
			case GREATER_EQUAL:
				return 8;
			case EQUAL:
				return 7;
			case NOT_EQUAL:
				return 7;
			case '&':
				return 6;
			case '^':
				return 5;
			case '|':
				return 4;
			case AND_AND:
				return 3;
			case OR_OR:
				return 2;
			case '?':
				return 1;
			default:
				// System.out.println("Unrecognised operator " + op);
				return 0;
		}
	}

	private int expr_char(Token token) {
		Object value = token.getValue();
		if (value instanceof Character) return (Character) value;
		String text = String.valueOf(value);
		if (text.length() == 0) return 0;
		return text.charAt(0);
	}

	@Nonnull
	private NumberValue expr(int priority) throws IOException, LexerException {
		/*
		 * (new Exception("expr(" + priority + ") called")).printStackTrace();
		 */

		Token tok = expr_token();
		NumberValue lhs, rhs;

		// System.out.println("Expr lhs token is " + tok);
		switch (tok.getType()) {
			case '(':
				lhs = expr(0);
				tok = expr_token();
				if (tok.getType() != ')') {
					expr_untoken(tok);
					error(tok, "Missing ) in expression. Got " + tok.getText());
					return NumberValue.INTEGER_ZERO;
				}
				break;

			case '~':
				lhs = expr(11).not();
				break;
			case '!':
				lhs = expr(11).equals(0) ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
				break;
			case '-':
				lhs = expr(11).negate();
				break;
			case NUMBER: {
				lhs = tok.getValue(NumberToken.class).value();
				break;
			}
			case CHARACTER:
				lhs = NumberValue.of(expr_char(tok));
				break;
			case IDENTIFIER:
				if (warnings.contains(Warning.UNDEF))
					warning(tok, "Undefined token '" + tok.getText() + "' encountered in conditional.");
				lhs = NumberValue.INTEGER_ZERO;
				break;

			default:
				expr_untoken(tok);
				error(tok, "Bad token in expression: " + tok.getText());
				return NumberValue.INTEGER_ZERO;
		}

		while (true) {
			// System.out.println("expr: lhs is " + lhs + ", pri = " + priority);
			Token op = expr_token();
			int pri = expr_priority(op);    /* 0 if not a binop. */

			if (pri == 0 || priority >= pri) {
				expr_untoken(op);
				break;
			}
			rhs = expr(pri);
			// System.out.println("rhs token is " + rhs);
			switch (op.getType()) {
				case '/':
					if (rhs.equals(0)) {
						error(op, "Division by zero");
						lhs = NumberValue.INTEGER_ZERO;
					} else {
						lhs = lhs.divide(rhs);
					}
					break;
				case '%':
					if (rhs.equals(0)) {
						error(op, "Modulus by zero");
						lhs = NumberValue.INTEGER_ZERO;
					} else {
						lhs = lhs.remainder(rhs);
					}
					break;
				case '*':
					lhs = lhs.multiply(rhs);
					break;
				case '+':
					lhs = lhs.add(rhs);
					break;
				case '-':
					lhs = lhs.subtract(rhs);
					break;
				case '<':
					lhs = lhs.compareTo(rhs) < 0 ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
					break;
				case '>':
					lhs = lhs.compareTo(rhs) > 0 ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
					break;
				case '&':
					lhs = lhs.and(rhs);
					break;
				case '^':
					lhs = lhs.xor(rhs);
					break;
				case '|':
					lhs = lhs.or(rhs);
					break;

				case LEFT_SHIFT:
					lhs = lhs.shiftLeft(rhs);
					break;
				case RIGHT_SHIFT:
					lhs = lhs.shiftRight(rhs);
					break;
				case LESS_EQUAL:
					lhs = lhs.compareTo(rhs) <= 0 ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
					break;
				case GREATER_EQUAL:
					lhs = lhs.compareTo(rhs) >= 0 ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
					break;
				case EQUAL:
					lhs = lhs.equals(rhs) ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
					break;
				case NOT_EQUAL:
					lhs = !lhs.equals(rhs) ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
					break;
				case AND_AND:
					lhs = !lhs.equals(0) && !rhs.equals(0) ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
					break;
				case OR_OR:
					lhs = !lhs.equals(0) || !rhs.equals(0) ? NumberValue.INTEGER_POSITIVE_ONE : NumberValue.INTEGER_ZERO;
					break;

				case '?': {
					tok = expr_token();
					if (tok.getType() != ':') {
						expr_untoken(tok);
						error(tok, "Missing : in conditional expression. Got " + tok.getText());
						return NumberValue.INTEGER_ZERO;
					}
					// do not simplify this! always need to consume the token!
					final NumberValue falseResult = expr(0);
					lhs = !lhs.equals(0) ? rhs : falseResult;
					break;
				}

				default:
					error(op, "Unexpected operator " + op.getText());
					return NumberValue.INTEGER_ZERO;

			}
		}

		/*
		 * (new Exception("expr returning " + lhs)).printStackTrace();
		 */
		// System.out.println("expr returning " + lhs);
		return lhs;
	}

	@Nonnull
	private Token toWhitespace(@Nonnull Token tok) {
		String text = tok.getText();
		int len = text.length();
		boolean cr = false;
		int nls = 0;

		for (int i = 0; i < len; i++) {
			char c = text.charAt(i);
			if (c == '\r') {
				cr = true;
				nls++;
			} else if (c == '\n') {
				if (cr) {
					cr = false;
					continue;
				}
				//cr = false;
				nls++;
			} else if (c == '\u2028' || c == '\u2029' || c == '\u000B' || c == '\u000C' || c == '\u0085') {
				cr = false;
				nls++;
			}
		}

		char[] cbuf = new char[nls];
		Arrays.fill(cbuf, '\n');
		return new Token(WHITESPACE,
				tok.getLine(), tok.getColumn(),
				new String(cbuf));
	}

	@Nullable
	private Token parseDirective() throws IOException, LexerException {
		final Token token = sourceGetTokenSkipWS();
		// (new Exception("here")).printStackTrace();
		if (token.getType() == NEW_LINE) {
			return null;
		} else if (token.getType() != IDENTIFIER) {
			error(token, "Preprocessor directive command is not a identifier " + token);
			return sourceSkipLine(false);
		}
		switch (token.getText()) {
			case "define":
				return stateIsActive() ? parseDefineDirective() : sourceSkipLine(false);
			case "undef":
				return stateIsActive() ? parseUndefDirective() : sourceSkipLine(false);
			case "include":
				return stateIsActive() ? parseIncludeDirective(false) : sourceSkipLine(false);
			case "include_next":
				if (stateIsActive()) {
					if (getFeature(Feature.INCLUDENEXT)) return parseIncludeDirective(true);
					error(token, "Directive include_next not enabled");
				}
				return sourceSkipLine(false);
			case "warning":
			case "error":
				if (!stateIsActive()) return sourceSkipLine(false);
				parseErrorDirective(token);
				return null;
			case "if":
				statePush();
				if (stateIsActive()) {
					this.expr_token = null;
					states.peek().setActive(!expr(0).equals(0));
					final Token expressionToken = expr_token();    /* unget */
					return expressionToken.getType() == NEW_LINE ? expressionToken : sourceSkipLine(true);
				}
				return sourceSkipLine(false);
			case "elif": {
				@Nonnull State state = states.peek();
				if (state.sawElse()) {
					error(token, "#elif after #else");
					return sourceSkipLine(false);
				} else if (!state.isParentActive()) {
					/* Nested in skipped 'if' */
					return sourceSkipLine(false);
				} else if (state.isActive()) {
					/* The 'if' part got executed. */
					state.deactivateParent();
					/* This is like # else # if but with
					 * only one # end. */
					state.setActive(false);
					return sourceSkipLine(false);
				} else {
					this.expr_token = null;
					state.setActive(!expr(0).equals(0));
					final Token expressionToken = expr_token();    /* unget */
					return expressionToken.getType() == NEW_LINE ? expressionToken : sourceSkipLine(true);
				}
				// break;
			}
			case "else": {
				final State state = states.peek();
				if (state.sawElse()) {
					error(token, "#else after #else");
					return sourceSkipLine(false);
				} else {
					state.setSawElse();
					state.setActive(!state.isActive());
					return sourceSkipLine(warnings.contains(Warning.ENDIF_LABELS));
				}
			}
			case "ifdef":
				statePush();
				if (stateIsActive()) {
					final Token macroName = sourceGetTokenSkipWS();
					if (macroName.getType() == IDENTIFIER) {
						states.peek().setActive(globalMacros.containsKey(macroName.getText()));
						return sourceSkipLine(true);
					}
					error(macroName, "Expected identifier, not " + macroName);
				}
				return sourceSkipLine(false);
			case "ifndef":
				statePush();
				if (stateIsActive()) {
					final Token macroName = sourceGetTokenSkipWS();
					if (macroName.getType() == IDENTIFIER) {
						states.peek().setActive(!globalMacros.containsKey(macroName.getText()));
						return sourceSkipLine(true);
					}
					error(macroName, "Expected identifier, not " + macroName);
				}
				return sourceSkipLine(false);
			case "endif":
				statePop();
				return sourceSkipLine(warnings.contains(Warning.ENDIF_LABELS));
			case "line":
				return sourceSkipLine(false);
			case "pragma":
				return stateIsActive() ? parsePragmaDirective() : sourceSkipLine(false);
			default:
				error(token, "Unknown preprocessor directive " + token);
				return sourceSkipLine(false);
		}
	}

	/**
	 * Returns the next preprocessor token.
	 *
	 * @return The next fully preprocessed token.
	 * @throws IOException    if an I/O error occurs.
	 * @throws LexerException if a preprocessing error occurs.
	 * @see Token
	 */
	@Nonnull
	public Token token() throws IOException, LexerException {
		while (true) {
			final Token token;
			if (!stateIsActive()) {
				final Source source = this.source;
				if (source == null) {
					final Token nextToken = nextInputSource();
					if (nextToken.getType() == P_LINE && !getFeature(Feature.LINEMARKERS)) continue;
					return nextToken;
				}

				try {
					/* XXX Tell lexer to ignore warnings. */
					source.setActive(false);
					token = sourceGetToken();
				} finally {
					/* XXX Tell lexer to stop ignoring warnings. */
					source.setActive(true);
				}
				switch (token.getType()) {
					case P_HASH:
					case NEW_LINE:
					case EOF:
						/* The preprocessor has to take action here. */
						break;
					case WHITESPACE:
						return token;
					case C_COMMENT:
					case CPP_COMMENT:
						// Patch up to preserve whitespace.
						if (getFeature(Feature.KEEPALLCOMMENTS)) return token;
						if (!stateIsActive()) return toWhitespace(token);
						if (getFeature(Feature.KEEPCOMMENTS)) return token;
						return toWhitespace(token);
					default:
						// Return NL to preserve whitespace.
						/* XXX This might lose a comment. */
						return sourceSkipLine(false);
				}
			} else {
				token = sourceGetToken();
			}

			int type = token.getType();
			if (type == IDENTIFIER) {
				final Macro macro = getMacro(token.getText());
				if (macro == null || source.isMacroExpanding(macro)) return token;
				if (parseMacroInUse(macro, token)) continue;
				return token;
			} else if (type == P_LINE) {
				if (getFeature(Feature.LINEMARKERS)) return token;
			} else if (type == INVALID) {
				error(token, String.valueOf(token.getValue()));
				return token;
			} else if (type == P_HASH) {
				final Token nextToken = parseDirective();
				if (nextToken != null) return nextToken;
			} else {
				return token;
			}
		}
	}

	@Nonnull
	private Token token_nonwhite() throws IOException, LexerException {
		while (true) {
			final Token token = token();
			final int type = token.getType();
			if (type != C_COMMENT && type != CPP_COMMENT && type != WHITESPACE) {
				return token;
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		Source source = this.source;
		while (source != null) {
			buf.append(" -> ").append(source).append("\n");
			source = source.getParent();
		}

		Map<String, Macro> macros = new TreeMap<>(getGlobalMacros());
		for (Macro macro : macros.values()) {
			buf.append("#").append("macro ").append(macro).append("\n");
		}

		return buf.toString();
	}

	@Override
	public void close() throws IOException {
		{
			Source s = source;
			while (s != null) {
				s.close();
				s = s.getParent();
			}
		}
		for (Source s : inputs) {
			s.close();
		}
	}

	/**
	 * Features of the Preprocessor, which may be enabled or disabled.
	 */
	public enum Feature {

		/**
		 * Supports ANSI digraphs.
		 */
		DIGRAPHS,
		/**
		 * Supports ANSI trigraphs.
		 */
		TRIGRAPHS,
		/**
		 * Outputs linemarker tokens.
		 */
		LINEMARKERS,
		/**
		 * Reports tokens of type INVALID as errors.
		 */
		CSYNTAX,
		/**
		 * Preserves comments in the lexed output. Like cpp -C
		 */
		KEEPCOMMENTS,
		/**
		 * Preserves comments in the lexed output, even when inactive.
		 */
		KEEPALLCOMMENTS,
		DEBUG,
		/**
		 * Supports lexing of objective-C.
		 */
		OBJCSYNTAX,
		INCLUDENEXT,
		/**
		 * Random extensions.
		 */
		PRAGMA_ONCE
	}

	/**
	 * Warning classes which may optionally be emitted by the Preprocessor.
	 */
	public enum Warning {
		TRIGRAPHS,
		// TRADITIONAL,
		IMPORT,
		UNDEF,
		UNUSED_MACROS,
		ENDIF_LABELS,
		ERROR,
		// SYSTEM_HEADERS
	}
}
