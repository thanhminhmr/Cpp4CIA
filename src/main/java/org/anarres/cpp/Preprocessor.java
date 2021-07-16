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
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import static org.anarres.cpp.Token.CHARACTER;
import static org.anarres.cpp.Token.CPP_COMMENT;
import static org.anarres.cpp.Token.C_COMMENT;
import static org.anarres.cpp.Token.ELLIPSIS;
import static org.anarres.cpp.Token.EOF;
import static org.anarres.cpp.Token.EQ;
import static org.anarres.cpp.Token.GE;
import static org.anarres.cpp.Token.HASH;
import static org.anarres.cpp.Token.HEADER;
import static org.anarres.cpp.Token.IDENTIFIER;
import static org.anarres.cpp.Token.INVALID;
import static org.anarres.cpp.Token.LAND;
import static org.anarres.cpp.Token.LE;
import static org.anarres.cpp.Token.LOR;
import static org.anarres.cpp.Token.LSH;
import static org.anarres.cpp.Token.M_ARG;
import static org.anarres.cpp.Token.M_PASTE;
import static org.anarres.cpp.Token.M_STRING;
import static org.anarres.cpp.Token.NE;
import static org.anarres.cpp.Token.NL;
import static org.anarres.cpp.Token.NUMBER;
import static org.anarres.cpp.Token.PASTE;
import static org.anarres.cpp.Token.P_LINE;
import static org.anarres.cpp.Token.RSH;
import static org.anarres.cpp.Token.STRING;
import static org.anarres.cpp.Token.WHITESPACE;

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
public class Preprocessor implements Closeable {

	private static final Source INTERNAL = new Source() {
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
		public String getName() {
			return "internal source";
		}
	};
	// standard macro
	private static final Macro __LINE__ = new Macro(INTERNAL, "__LINE__");
	private static final Macro __FILE__ = new Macro(INTERNAL, "__FILE__");
	private static final Macro __COUNTER__ = new Macro(INTERNAL, "__COUNTER__");
	private static final Map<String, Macro> DEFAULT_MACRO = Map.ofEntries(
			Map.entry("__LINE__", __LINE__),
			Map.entry("__FILE__", __FILE__),
			Map.entry("__COUNTER__", __COUNTER__),
			// gcc specific
			Map.entry("__attribute__", new Macro(INTERNAL, "__attribute__", List.of("unused"))),
			// msvc specific
			Map.entry("__cdecl", new Macro(INTERNAL, "__cdecl")),
			Map.entry("__clrcall", new Macro(INTERNAL, "__clrcall")),
			Map.entry("__stdcall", new Macro(INTERNAL, "__stdcall")),
			Map.entry("__fastcall", new Macro(INTERNAL, "__fastcall")),
			Map.entry("__thiscall", new Macro(INTERNAL, "__thiscall")),
			Map.entry("__vectorcall", new Macro(INTERNAL, "__vectorcall")),
			Map.entry("__declspec", new Macro(INTERNAL, "__declspec", List.of("unused")))
	);

	private final Queue<Source> inputs = new LinkedList<>();

	/* The fundamental engine. */
	private final Map<String, Macro> macros = new HashMap<>(DEFAULT_MACRO);
	private final Stack<State> states = new Stack<>();
	private Source source = null;

	/* Miscellaneous support. */
	private int counter = 0;
	private final Set<Path> pragmaOnceFiles = new HashSet<>();
	private final Set<Path> includes = new HashSet<>();
	private final Set<Path> notExists = new HashSet<>();

	/* Support junk to make it work like cpp */
	private List<Path> quoteIncludePath = new ArrayList<>();    /* -iquote */
	private List<Path> sysIncludePath = new ArrayList<>();        /* -I */
	private List<Path> frameworksPath = new ArrayList<>();

	private final Set<Feature> features = EnumSet.noneOf(Feature.class);
	private final Set<Warning> warnings = EnumSet.noneOf(Warning.class);
	private FileSystem filesystem = FileSystems.getDefault();
	private PreprocessorListener listener = null;

	public Preprocessor() {
		states.push(new State());
	}

	public Preprocessor(@Nonnull Source initial) {
		this();
		addInput(initial);
	}

	/**
	 * Sets the FileSystem used by this Preprocessor.
	 */
	public void setFileSystem(@Nonnull FileSystem filesystem) {
		this.filesystem = filesystem;
	}

	/**
	 * Returns the FileSystem used by this Preprocessor.
	 */
	@Nonnull
	public FileSystem getFileSystem() {
		return filesystem;
	}

	/**
	 * Sets the PreprocessorListener which handles events for
	 * this Preprocessor.
	 * <p>
	 * The listener is notified of warnings, errors and source
	 * changes, amongst other things.
	 */
	public void setListener(@Nonnull PreprocessorListener listener) {
		this.listener = listener;
		Source s = source;
		while (s != null) {
			// s.setListener(listener);
			s.init(this);
			s = s.getParent();
		}
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
	public void addFeature(@Nonnull Feature f) {
		features.add(f);
	}

	/**
	 * Adds features to the feature-set of this Preprocessor.
	 */
	public void addFeatures(@Nonnull Collection<Feature> f) {
		features.addAll(f);
	}

	/**
	 * Adds features to the feature-set of this Preprocessor.
	 */
	public void addFeatures(Feature... f) {
		addFeatures(Arrays.asList(f));
	}

	/**
	 * Returns true if the given feature is in
	 * the feature-set of this Preprocessor.
	 */
	public boolean getFeature(@Nonnull Feature f) {
		return features.contains(f);
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
	public void addWarning(@Nonnull Warning w) {
		warnings.add(w);
	}

	/**
	 * Adds warnings to the warning-set of this Preprocessor.
	 */
	public void addWarnings(@Nonnull Collection<Warning> w) {
		warnings.addAll(w);
	}

	/**
	 * Returns true if the given warning is in
	 * the warning-set of this Preprocessor.
	 */
	public boolean getWarning(@Nonnull Warning w) {
		return warnings.contains(w);
	}

	/**
	 * Adds input for the Preprocessor.
	 * <p>
	 * Inputs are processed in the order in which they are added.
	 */
	public void addInput(@Nonnull Source source) {
		source.init(this);
		inputs.add(source);
	}

	/**
	 * Handles an error.
	 * <p>
	 * If a PreprocessorListener is installed, it receives the
	 * error. Otherwise, an exception is thrown.
	 */
	protected void error(int line, int column, @Nonnull String msg) throws LexerException {
		if (listener != null)
			listener.handleError(source, line, column, msg);
		else
			throw new LexerException("Error at " + line + ":" + column + ": " + msg);
	}

	/**
	 * Handles an error.
	 * <p>
	 * If a PreprocessorListener is installed, it receives the
	 * error. Otherwise, an exception is thrown.
	 *
	 * @see #error(int, int, String)
	 */
	protected void error(@Nonnull Token tok, @Nonnull String msg) throws LexerException {
		error(tok.getLine(), tok.getColumn(), msg);
	}

	/**
	 * Handles a warning.
	 * <p>
	 * If a PreprocessorListener is installed, it receives the
	 * warning. Otherwise, an exception is thrown.
	 */
	protected void warning(int line, int column, @Nonnull String msg) throws LexerException {
		if (warnings.contains(Warning.ERROR))
			error(line, column, msg);
		else if (listener != null)
			listener.handleWarning(source, line, column, msg);
		else
			throw new LexerException("Warning at " + line + ":" + column + ": " + msg);
	}

	/**
	 * Handles a warning.
	 * <p>
	 * If a PreprocessorListener is installed, it receives the
	 * warning. Otherwise, an exception is thrown.
	 *
	 * @see #warning(int, int, String)
	 */
	protected void warning(@Nonnull Token tok, @Nonnull String msg) throws LexerException {
		warning(tok.getLine(), tok.getColumn(), msg);
	}

	/**
	 * Adds a Macro to this Preprocessor.
	 * <p>
	 * The given {@link Macro} object encapsulates both the name
	 * and the expansion.
	 *
	 * @throws LexerException if the definition fails or is otherwise illegal.
	 */
	public void addMacro(@Nonnull Macro m) throws LexerException {
		// System.out.println("Macro " + m);
		String name = m.getName();
		/* Already handled as a source error in macro(). */
		if ("defined".equals(name))
			throw new LexerException("Cannot redefine name 'defined'");
		macros.put(m.getName(), m);
	}

	/**
	 * Defines the given name as a macro.
	 * <p>
	 * The String value is lexed into a token stream, which is
	 * used as the macro expansion.
	 *
	 * @throws LexerException if the definition fails or is otherwise illegal.
	 */
	public void addMacro(@Nonnull String name, @Nonnull String value) throws LexerException {
		try {
			Macro m = new Macro(name);
			StringLexerSource s = new StringLexerSource(value);
			for (; ; ) {
				Token tok = s.token();
				if (tok.getType() == EOF)
					break;
				m.addToken(tok);
			}
			addMacro(m);
		} catch (IOException e) {
			throw new LexerException(e);
		}
	}

	/**
	 * Defines the given name as a macro, with the value <code>1</code>.
	 * <p>
	 * This is a convnience method, and is equivalent to
	 * <code>addMacro(name, "1")</code>.
	 *
	 * @throws LexerException if the definition fails or is otherwise illegal.
	 */
	public void addMacro(@Nonnull String name) throws LexerException {
		addMacro(name, "1");
	}

	/**
	 * Sets the user include path used by this Preprocessor.
	 */
	/* Note for future: Create an IncludeHandler? */
	public void setQuoteIncludePath(@Nonnull List<Path> path) {
		this.quoteIncludePath = new ArrayList<>(path);
	}

	/**
	 * Returns the user include-path of this Preprocessor.
	 * <p>
	 * This list may be freely modified by user code.
	 */
	@Nonnull
	public List<Path> getQuoteIncludePath() {
		return quoteIncludePath;
	}

	/**
	 * Sets the system include path used by this Preprocessor.
	 */
	/* Note for future: Create an IncludeHandler? */
	public void setSystemIncludePath(@Nonnull List<Path> path) {
		this.sysIncludePath = new ArrayList<>(path);
	}

	/**
	 * Returns the system include-path of this Preprocessor.
	 * <p>
	 * This list may be freely modified by user code.
	 */
	@Nonnull
	public List<Path> getSystemIncludePath() {
		return sysIncludePath;
	}

	/**
	 * Sets the Objective-C frameworks path used by this Preprocessor.
	 */
	/* Note for future: Create an IncludeHandler? */
	public void setFrameworksPath(@Nonnull List<Path> path) {
		this.frameworksPath = new ArrayList<>(path);
	}

	/**
	 * Returns the Objective-C frameworks path used by this
	 * Preprocessor.
	 * <p>
	 * This list may be freely modified by user code.
	 */
	@Nonnull
	public List<Path> getFrameworksPath() {
		return frameworksPath;
	}

	/**
	 * Returns the Map of Macros parsed during the run of this
	 * Preprocessor.
	 *
	 * @return The {@link Map} of macros currently defined.
	 */
	@Nonnull
	public Map<String, Macro> getMacros() {
		return macros;
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
		return macros.get(name);
	}

	/**
	 * Returns the list of {@link Path files} which have been
	 * included by this Preprocessor.
	 * <p>
	 * This does not include any {@link Source} provided to {@link #addInput(Source)}.
	 */
	@Nonnull
	public Set<Path> getIncludes() {
		return includes;
	}

	/* States */
	private void push_state() {
		State top = states.peek();
		states.push(new State(top));
	}

	private void pop_state() throws LexerException {
		State s = states.pop();
		if (states.isEmpty()) {
			error(0, 0, "#endif without #if");
			states.push(s);
		}
	}

	private boolean isActive() {
		State state = states.peek();
		return state.isParentActive() && state.isActive();
	}


	/* Sources */

	/**
	 * Returns the top Source on the input stack.
	 *
	 * @return the top Source on the input stack.
	 * @see Source
	 * @see #push_source(Source, boolean)
	 * @see #pop_source()
	 */
	// @CheckForNull
	protected Source getSource() {
		return source;
	}

	/**
	 * Pushes a Source onto the input stack.
	 *
	 * @param source  the new Source to push onto the top of the input stack.
	 * @param autoPop if true, the Source is automatically removed from the input stack at EOF.
	 * @see #getSource()
	 * @see #pop_source()
	 */
	protected void push_source(@Nonnull Source source, boolean autoPop) {
		source.init(this);
		source.setParent(this.source, autoPop);
		// source.setListener(listener);
		if (listener != null && this.source != null) {
			listener.handleSourceChange(this.source, SourceChangeEvent.SUSPEND);
		}
		this.source = source;
		if (listener != null) {
			listener.handleSourceChange(this.source, SourceChangeEvent.PUSH);
		}
	}

	/**
	 * Pops a Source from the input stack.
	 *
	 * @throws IOException if an I/O error occurs.
	 * @see #getSource()
	 * @see #push_source(Source, boolean)
	 */
	@Nullable
	protected Token pop_source() throws IOException {
		if (listener != null) {
			listener.handleSourceChange(this.source, SourceChangeEvent.POP);
		}
		Source oldSource = this.source;
		this.source = oldSource.getParent();
		/* Always a noop unless called externally. */
		oldSource.close();
		if (source == null) return next_source();
		if (listener != null) {
			listener.handleSourceChange(source, SourceChangeEvent.RESUME);
		}
		if (getFeature(Feature.LINEMARKERS) && oldSource.isNumbered()) {
			/* We actually want 'did the nested source
			 * contain a newline token', which isNumbered()
			 * approximates. This is not perfect, but works. */
			return line_token(source.getLine(), source.getName(), " 2");
		}
		return null;
	}

	/**
	 * Get the next source from the input queue
	 */
	@Nonnull
	private Token next_source() {
		if (inputs.isEmpty()) return Token.eof;
		final Source source = inputs.remove();
		final Path path = source.getPath();
		if (path == null || includes.add(path)) {

		}
		push_source(source, true);
		return line_token(source.getLine(), source.getName(), " 1");
	}

	/* XXX Make this include the NL, and make all cpp directives eat
	 * their own NL. */
	@Nonnull
	private Token line_token(int line, @Nullable String name, @Nonnull String extra) {
		StringBuilder buf = new StringBuilder();
		buf.append("#line ").append(line).append(" \"");
		/* XXX This call to escape(name) is correct but ugly. */
		if (name == null) {
			buf.append("<no file>");
		} else {
			MacroTokenSource.escape(buf, name);
		}
		buf.append("\"").append(extra).append("\n");
		return new Token(P_LINE, line, 0, buf.toString(), null);
	}

	/* Source tokens */
	private Token source_pushback_token;

	private Token nextSourceTokenCheckPushback() throws IOException, LexerException {
		if (source_pushback_token != null) {
			final Token token = source_pushback_token;
			this.source_pushback_token = null;
			return token;
		}
		if (source == null) {
			final Token token = next_source();
			if (token.getType() != P_LINE || getFeature(Feature.LINEMARKERS)) return token;
		}
		return source != null ? source.token() : Token.eof;
	}

	@Nonnull
	private Token source_token() throws IOException, LexerException {
		while (true) {
			final Token token = nextSourceTokenCheckPushback();
			if (token.getType() == EOF && source.isAutoPop()) {
				final Token lineMark = pop_source();
				if (lineMark != null) return lineMark;
				continue;
			}
			return token;
		}
	}

	private void source_untoken(Token tok) {
		if (this.source_pushback_token != null) {
			throw new IllegalStateException("Cannot return two tokens");
		}
		this.source_pushback_token = tok;
	}

	private boolean isWhite(Token tok) {
		int type = tok.getType();
		return (type == WHITESPACE) || (type == C_COMMENT) || (type == CPP_COMMENT);
	}

	/**
	 * Returns an NL or an EOF token.
	 * <p>
	 * The metadata on the token will be correct, which is better
	 * than generating a new one.
	 * <p>
	 * This method can, as of recent patches, return a P_LINE token.
	 */
	private Token source_skipLine(boolean white) throws IOException, LexerException {
		while (true) {
			final Token token = nextSourceTokenCheckPushback();
			final int type = token.getType();
			if (type == EOF) {
				warning(token, "No newline before end of file");
				if (source.isAutoPop()) {
					final Token lineMark = pop_source();
					if (lineMark != null) return lineMark;
				}
				return token;
			} else if (type == NL) {
				return token;
			} else if (white && type != C_COMMENT && type != CPP_COMMENT && type != WHITESPACE) {
				warning(token, "Unexpected nonwhite token");
			}
		}
	}

	/* processes and expands a macro. */
	private boolean macro(Macro m, Token orig) throws IOException, LexerException {
		Token tok;
		List<Macro.Argument> args;

		// System.out.println("pp: expanding " + m);
		if (m.isFunctionLike()) {
			OPEN:
			for (; ; ) {
				tok = source_token();
				// System.out.println("pp: open: token is " + tok);
				switch (tok.getType()) {
					case WHITESPACE:    /* XXX Really? */

					case C_COMMENT:
					case CPP_COMMENT:
					case NL:
						break;    /* continue */

					case '(':
						break OPEN;
					default:
						source_untoken(tok);
						return false;
				}
			}

			// tok = expanded_token_nonwhite();
			tok = source_token_nonwhite();

			/* We either have, or we should have args.
			 * This deals elegantly with the case that we have
			 * one empty arg. */
			if (tok.getType() != ')' || m.getArgs() > 0) {
				args = new ArrayList<Macro.Argument>();

				Macro.Argument arg = new Macro.Argument();
				int depth = 0;
				boolean space = false;

				ARGS:
				for (; ; ) {
					// System.out.println("pp: arg: token is " + tok);
					switch (tok.getType()) {
						case EOF:
							error(tok, "EOF in macro args");
							return false;

						case ',':
							if (depth == 0) {
								if (m.isVariadic()
										&& /* We are building the last arg. */ args.size() == m.getArgs() - 1) {
									/* Just add the comma. */
									arg.addToken(tok);
								} else {
									args.add(arg);
									arg = new Macro.Argument();
								}
							} else {
								arg.addToken(tok);
							}
							space = false;
							break;
						case ')':
							if (depth == 0) {
								args.add(arg);
								break ARGS;
							} else {
								depth--;
								arg.addToken(tok);
							}
							space = false;
							break;
						case '(':
							depth++;
							arg.addToken(tok);
							space = false;
							break;

						case WHITESPACE:
						case C_COMMENT:
						case CPP_COMMENT:
						case NL:
							/* Avoid duplicating spaces. */
							space = true;
							break;

						default:
							/* Do not put space on the beginning of
							 * an argument token. */
							if (space && !arg.isEmpty())
								arg.addToken(Token.whitespace);
							arg.addToken(tok);
							space = false;
							break;

					}
					// tok = expanded_token();
					tok = source_token();
				}
				/* space may still be true here, thus trailing space
				 * is stripped from arguments. */

				if (args.size() != m.getArgs()) {
					if (m.isVariadic()) {
						if (args.size() == m.getArgs() - 1) {
							args.add(new Macro.Argument());
						} else {
							error(tok,
									"variadic macro " + m.getName()
											+ " has at least " + (m.getArgs() - 1) + " parameters "
											+ "but given " + args.size() + " args");
							return false;
						}
					} else {
						error(tok,
								"macro " + m.getName()
										+ " has " + m.getArgs() + " parameters "
										+ "but given " + args.size() + " args");
						/* We could replay the arg tokens, but I
						 * note that GNU cpp does exactly what we do,
						 * i.e. output the macro name and chew the args.
						 */
						return false;
					}
				}

				for (Macro.Argument a : args) {
					a.expand(this);
				}

				// System.out.println("Macro " + m + " args " + args);
			} else {
				/* nargs == 0 and we (correctly) got () */
				args = null;
			}

		} else {
			/* Macro without args. */
			args = null;
		}

		if (m == __LINE__) {
			push_source(new FixedTokenSource(
					new Token(NUMBER,
							orig.getLine(), orig.getColumn(),
							Integer.toString(orig.getLine()),
							new NumericValue(10, Integer.toString(orig.getLine())))), true);
		} else if (m == __FILE__) {
			StringBuilder buf = new StringBuilder("\"");
			String name = getSource().getName();
			if (name == null)
				name = "<no file>";
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				switch (c) {
					case '\\':
						buf.append("\\\\");
						break;
					case '"':
						buf.append("\\\"");
						break;
					default:
						buf.append(c);
						break;
				}
			}
			buf.append("\"");
			String text = buf.toString();
			push_source(new FixedTokenSource(
					new Token(STRING,
							orig.getLine(), orig.getColumn(),
							text, text)), true);
		} else if (m == __COUNTER__) {
			/* This could equivalently have been done by adding
			 * a special Macro subclass which overrides getTokens(). */
			int value = this.counter++;
			push_source(new FixedTokenSource(
					new Token(NUMBER,
							orig.getLine(), orig.getColumn(),
							Integer.toString(value),
							new NumericValue(10, Integer.toString(value)))), true);
		} else {
			push_source(new MacroTokenSource(m, args), true);
		}

		return true;
	}

	private Token source_token_nonwhite() throws IOException, LexerException {
		while (true) {
			final Token token = source_token();
			final int type = token.getType();
			if (type != WHITESPACE && type != C_COMMENT && type != CPP_COMMENT) return token;
		}
	}

	/**
	 * Expands an argument.
	 */
	/* I'd rather this were done lazily, but doing so breaks spec. */
	@Nonnull
	List<Token> expand(@Nonnull List<Token> arg) throws IOException, LexerException {
		List<Token> expansion = new ArrayList<>();
		boolean space = false;

		push_source(new FixedTokenSource(arg), false);

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
		pop_source();

		return expansion;
	}

	/* processes a #define directive */
	private Token parseDefineDirective() throws IOException, LexerException {
		final Token token = source_token_nonwhite();
		if (token.getType() != IDENTIFIER) {
			error(token, "Expected identifier instead of " + token);
			return source_skipLine(false);
		}

		/* if predefined */
		final String name = token.getText();
		if ("defined".equals(name)) {
			error(token, "Cannot redefine name 'defined'");
			return source_skipLine(false);
		}

		final Macro macro = new Macro(getSource(), name);
		final List<String> args = new ArrayList<>();

		Token openToken = source_token();
		if (openToken.getType() == '(') {
			Token nextToken = source_token_nonwhite();
			if (nextToken.getType() != ')') {
				ARGS:
				for (; ; ) {
					switch (nextToken.getType()) {
						case IDENTIFIER:
							args.add(nextToken.getText());
							break;
						case ELLIPSIS:
							// Unnamed Variadic macro
							args.add("__VA_ARGS__");
							// We just named the ellipsis, but we unget the token
							// to allow the ELLIPSIS handling below to process it.
							source_untoken(nextToken);
							break;
						case NL:
						case EOF:
							error(nextToken,
									"Unterminated macro parameter list");
							return nextToken;
						default:
							error(nextToken,
									"error in macro parameters: "
											+ nextToken.getText());
							return source_skipLine(false);
					}
					nextToken = source_token_nonwhite();
					switch (nextToken.getType()) {
						case ',':
							break;
						case ELLIPSIS:
							nextToken = source_token_nonwhite();
							if (nextToken.getType() != ')')
								error(nextToken,
										"ellipsis must be on last argument");
							macro.setVariadic(true);
							break ARGS;
						case ')':
							break ARGS;

						case NL:
						case EOF:
							/* Do not skip line. */
							error(nextToken,
									"Unterminated macro parameters");
							return nextToken;
						default:
							error(nextToken,
									"Bad token in macro parameters: "
											+ nextToken.getText());
							return source_skipLine(false);
					}
					nextToken = source_token_nonwhite();
				}
			}

			macro.setArgs(args);
		} else {
			/* For searching. */
			source_untoken(openToken);
		}

		/* Get an expansion for the macro, using indexOf. */
		boolean space = false;
		boolean paste = false;
		int idx;

		/* Ensure no space at start. */
		openToken = source_token_nonwhite();
		EXPANSION:
		for (; ; ) {
			switch (openToken.getType()) {
				case EOF:
					break EXPANSION;
				case NL:
					break EXPANSION;

				case C_COMMENT:
				case CPP_COMMENT:
					/* XXX This is where we implement GNU's cpp -CC. */
					// break;
				case WHITESPACE:
					if (!paste)
						space = true;
					break;

				/* Paste. */
				case PASTE:
					space = false;
					paste = true;
					macro.addPaste(new Token(M_PASTE,
							openToken.getLine(), openToken.getColumn(),
							"#" + "#", null));
					break;

				/* Stringify. */
				case '#':
					if (space)
						macro.addToken(Token.whitespace);
					space = false;
					Token la = source_token_nonwhite();
					if (la.getType() == IDENTIFIER
							&& ((idx = args.indexOf(la.getText())) != -1)) {
						macro.addToken(new Token(M_STRING,
								la.getLine(), la.getColumn(),
								"#" + la.getText(),
								idx));
					} else {
						macro.addToken(openToken);
						/* Allow for special processing. */
						source_untoken(la);
					}
					break;

				case IDENTIFIER:
					if (space)
						macro.addToken(Token.whitespace);
					space = false;
					paste = false;
					idx = args.indexOf(openToken.getText());
					if (idx == -1)
						macro.addToken(openToken);
					else
						macro.addToken(new Token(M_ARG,
								openToken.getLine(), openToken.getColumn(),
								openToken.getText(),
								idx));
					break;

				default:
					if (space)
						macro.addToken(Token.whitespace);
					space = false;
					paste = false;
					macro.addToken(openToken);
					break;
			}
			openToken = source_token();
		}

//        if (getFeature(Feature.DEBUG))
//            LOG.debug("Defined macro " + m);
		addMacro(macro);

		return openToken;    /* NL or EOF. */

	}

	@Nonnull
	private Token parseUndefDirective() throws IOException, LexerException {
		final Token token = source_token_nonwhite();
		if (token.getType() != IDENTIFIER) {
			error(token, "Expected identifier, not " + token);
			if (token.getType() == NL || token.getType() == EOF) return token;
		} else {
			final Macro macro = getMacro(token.getText());
			if (macro != null) {
				/* XXX error if predefined */
				macros.remove(macro.getName());
			}
		}
		return source_skipLine(true);
	}

	/**
	 * Attempts to include the given file.
	 * <p>
	 * User code may override this method to implement a virtual
	 * file system.
	 *
	 * @param file The Path to file to attempt to include.
	 * @return true if the file was successfully included, false otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	protected boolean include(@Nonnull Path file) throws IOException {
		final Path normalizedFile = file.normalize();
		if (pragmaOnceFiles.contains(normalizedFile)) return true;
		if (!notExists.contains(normalizedFile)) {
			if (includes.contains(normalizedFile)) {
				push_source(new FileLexerSource(normalizedFile), true);
				return true;
			} else if (Files.isRegularFile(normalizedFile)) {
				includes.add(normalizedFile);
				push_source(new FileLexerSource(normalizedFile), true);
				return true;
			} else {
				notExists.add(normalizedFile);
			}
		}
		return false;
	}

	/**
	 * Attempts to include a file from an include paths, by name.
	 *
	 * @param paths The list of virtual directories to search for the given name.
	 * @param name  The name of the file to attempt to include.
	 * @return true if the file was successfully included, false otherwise.
	 * @throws IOException if an I/O error occurs.
	 */
	protected boolean include(@Nonnull Iterable<Path> paths, @Nonnull String name, boolean next) throws IOException {
		for (final Path path : paths) {
			// Implement the next functionality here. This is actually
			// not exactly right because it should be based on the include
			// we are currently parsing, but it works _almost_ the same given
			// how limited the usage of #include_next is.
			if (includes.contains(path) && next) continue;

			if (include(path.resolve(name))) return true;
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
		// heuristic for absolute path
		if (name.startsWith("/") || name.startsWith("\\") || name.startsWith(":", 1)) {
			if (include(Path.of(name))) return;
		} else {
			if (!quoted) {
				int idx = name.indexOf('/');
				if (idx != -1) {
					String frameworkName = name.substring(0, idx);
					String headerName = name.substring(idx + 1);
					if (include(frameworksPath, frameworkName + ".framework/Headers/" + headerName, next)) return;
				}
			} else if (parent != null && include(parent.resolveSibling(name))
					|| include(quoteIncludePath, name, next)) {
				return;
			}
			if (include(sysIncludePath, name, next)) return;
		}
		error(line, 0, "File not found: " + name);
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
				return type != NL && type != EOF ? source_skipLine(false) : token;
			}

			String name = token.getValueAsString();
			final Token newlineToken = source_skipLine(true);

			/* Do the inclusion. */
			include(source.getPath(), token.getLine(), name, token.getText().startsWith("\""), next);

			return getFeature(Feature.LINEMARKERS) ? line_token(1, source.getName(), " 1") : newlineToken;
		} finally {
			lexer.setInclude(false);
		}
	}

	protected void pragma_once() throws IOException {
		if (!pragmaOnceFiles.add(source.getPath())) {
			final Token lineMarker = pop_source();
			// FixedTokenSource should never generate a linemarker on exit.
			if (lineMarker != null) push_source(new FixedTokenSource(lineMarker), true);
		}
	}

	protected void pragma(@Nonnull Token name, @Nonnull List<Token> value) throws IOException, LexerException {
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
		final Token name = source_token_nonwhite();
		{
			final int type = name.getType();
			if (type == EOF) {
				warning(name, "End of file in #pragma");
				return name;
			} else if (type == NL) {
				warning(name, "Empty #pragma");
				return name;
			} else if (type != IDENTIFIER) {
				warning(name, "Illegal #pragma " + name.getText());
				return source_skipLine(false);
			}
		}
		final List<Token> value = new ArrayList<>();
		boolean whitespace = false;
		while (true) {
			final Token token = source_token(); // cannot skip whitespace here, might be needed
			final int type = token.getType();
			if (type == C_COMMENT || type == CPP_COMMENT || type == WHITESPACE) {
				whitespace = true;
			} else if (type != EOF && type != NL) {
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
		Token tok = source_token_nonwhite();
		while (true) {
			final int type = tok.getType();
			if (type == NL || type == EOF) {
				break;
			} else {
				builder.append(tok.getText());
			}
			tok = source_token();
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
		for (; ; ) {
			Token tok = source_token();
			// System.out.println("Source token is " + tok);
			if (tok.getType() == IDENTIFIER) {
				Macro m = getMacro(tok.getText());
				if (m == null)
					return tok;
				if (source.isExpanding(m))
					return tok;
				if (macro(m, tok))
					continue;
			}
			return tok;
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
				Token la = source_token_nonwhite();
				boolean paren = false;
				if (la.getType() == '(') {
					paren = true;
					la = source_token_nonwhite();
				}

				// System.out.println("Core token is " + la);
				if (la.getType() != IDENTIFIER) {
					error(la, "defined() needs identifier, not " + la.getText());
					tok = new Token(NUMBER, la.getLine(), la.getColumn(), "0", new NumericValue(10, "0"));
				} else if (macros.containsKey(la.getText())) {
					// System.out.println("Found macro");
					tok = new Token(NUMBER, la.getLine(), la.getColumn(), "1", new NumericValue(10, "1"));
				} else {
					// System.out.println("Not found macro");
					tok = new Token(NUMBER, la.getLine(), la.getColumn(), "0", new NumericValue(10, "0"));
				}

				if (paren) {
					la = source_token_nonwhite();
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
			case LSH:
				return 9;
			case RSH:
				return 9;
			case '<':
				return 8;
			case '>':
				return 8;
			case LE:
				return 8;
			case GE:
				return 8;
			case EQ:
				return 7;
			case NE:
				return 7;
			case '&':
				return 6;
			case '^':
				return 5;
			case '|':
				return 4;
			case LAND:
				return 3;
			case LOR:
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
		if (value instanceof Character)
			return (Character) value;
		String text = String.valueOf(value);
		if (text.length() == 0)
			return 0;
		return text.charAt(0);
	}

	private long expr(int priority) throws IOException, LexerException {
		/*
		 * (new Exception("expr(" + priority + ") called")).printStackTrace();
		 */

		Token tok = expr_token();
		long lhs, rhs;

		// System.out.println("Expr lhs token is " + tok);
		switch (tok.getType()) {
			case '(':
				lhs = expr(0);
				tok = expr_token();
				if (tok.getType() != ')') {
					expr_untoken(tok);
					error(tok, "Missing ) in expression. Got " + tok.getText());
					return 0;
				}
				break;

			case '~':
				lhs = ~expr(11);
				break;
			case '!':
				lhs = expr(11) == 0 ? 1 : 0;
				break;
			case '-':
				lhs = -expr(11);
				break;
			case NUMBER:
				NumericValue value = (NumericValue) tok.getValue();
				lhs = value.longValue();
				break;
			case CHARACTER:
				lhs = expr_char(tok);
				break;
			case IDENTIFIER:
				if (warnings.contains(Warning.UNDEF))
					warning(tok, "Undefined token '" + tok.getText()
							+ "' encountered in conditional.");
				lhs = 0;
				break;

			default:
				expr_untoken(tok);
				error(tok,
						"Bad token in expression: " + tok.getText());
				return 0;
		}

		EXPR:
		for (; ; ) {
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
					if (rhs == 0) {
						error(op, "Division by zero");
						lhs = 0;
					} else {
						lhs = lhs / rhs;
					}
					break;
				case '%':
					if (rhs == 0) {
						error(op, "Modulus by zero");
						lhs = 0;
					} else {
						lhs = lhs % rhs;
					}
					break;
				case '*':
					lhs = lhs * rhs;
					break;
				case '+':
					lhs = lhs + rhs;
					break;
				case '-':
					lhs = lhs - rhs;
					break;
				case '<':
					lhs = lhs < rhs ? 1 : 0;
					break;
				case '>':
					lhs = lhs > rhs ? 1 : 0;
					break;
				case '&':
					lhs = lhs & rhs;
					break;
				case '^':
					lhs = lhs ^ rhs;
					break;
				case '|':
					lhs = lhs | rhs;
					break;

				case LSH:
					lhs = lhs << rhs;
					break;
				case RSH:
					lhs = lhs >> rhs;
					break;
				case LE:
					lhs = lhs <= rhs ? 1 : 0;
					break;
				case GE:
					lhs = lhs >= rhs ? 1 : 0;
					break;
				case EQ:
					lhs = lhs == rhs ? 1 : 0;
					break;
				case NE:
					lhs = lhs != rhs ? 1 : 0;
					break;
				case LAND:
					lhs = (lhs != 0) && (rhs != 0) ? 1 : 0;
					break;
				case LOR:
					lhs = (lhs != 0) || (rhs != 0) ? 1 : 0;
					break;

				case '?': {
					tok = expr_token();
					if (tok.getType() != ':') {
						expr_untoken(tok);
						error(tok, "Missing : in conditional expression. Got " + tok.getText());
						return 0;
					}
					long falseResult = expr(0);
					lhs = (lhs != 0) ? rhs : falseResult;
				}
				break;

				default:
					error(op, "Unexpected operator " + op.getText());
					return 0;

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
				cr = false;
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
		final Token token = source_token_nonwhite();
		// (new Exception("here")).printStackTrace();
		if (token.getType() == NL) {
			return null;
		} else if (token.getType() != IDENTIFIER) {
			error(token, "Preprocessor directive command is not a identifier " + token);
			return source_skipLine(false);
		}
		switch (token.getText()) {
			case "define":
				return isActive() ? parseDefineDirective() : source_skipLine(false);
			case "undef":
				return isActive() ? parseUndefDirective() : source_skipLine(false);
			case "include":
				return isActive() ? parseIncludeDirective(false) : source_skipLine(false);
			case "include_next":
				if (isActive()) {
					if (getFeature(Feature.INCLUDENEXT)) return parseIncludeDirective(true);
					error(token, "Directive include_next not enabled");
				}
				return source_skipLine(false);
			case "warning":
			case "error":
				if (!isActive()) return source_skipLine(false);
				parseErrorDirective(token);
				return null;
			case "if":
				push_state();
				if (isActive()) {
					this.expr_token = null;
					states.peek().setActive(expr(0) != 0);
					final Token expressionToken = expr_token();    /* unget */
					return expressionToken.getType() == NL ? expressionToken : source_skipLine(true);
				}
				return source_skipLine(false);
			case "elif": {
				State state = states.peek();
				if (state.sawElse()) {
					error(token, "#elif after #else");
					return source_skipLine(false);
				} else if (!state.isParentActive()) {
					/* Nested in skipped 'if' */
					return source_skipLine(false);
				} else if (state.isActive()) {
					/* The 'if' part got executed. */
					state.setParentActive(false);
					/* This is like # else # if but with
					 * only one # end. */
					state.setActive(false);
					return source_skipLine(false);
				} else {
					this.expr_token = null;
					state.setActive(expr(0) != 0);
					final Token expressionToken = expr_token();    /* unget */
					return expressionToken.getType() == NL ? expressionToken : source_skipLine(true);
				}
				// break;
			}
			case "else": {
				final State state = states.peek();
				if (state.sawElse()) {
					error(token, "#else after #else");
					return source_skipLine(false);
				} else {
					state.setSawElse();
					state.setActive(!state.isActive());
					return source_skipLine(warnings.contains(Warning.ENDIF_LABELS));
				}
			}
			case "ifdef":
				push_state();
				if (isActive()) {
					final Token macroName = source_token_nonwhite();
					if (macroName.getType() == IDENTIFIER) {
						states.peek().setActive(macros.containsKey(macroName.getText()));
						return source_skipLine(true);
					}
					error(macroName, "Expected identifier, not " + macroName);
				}
				return source_skipLine(false);
			case "ifndef":
				push_state();
				if (isActive()) {
					final Token macroName = source_token_nonwhite();
					if (macroName.getType() == IDENTIFIER) {
						states.peek().setActive(!macros.containsKey(macroName.getText()));
						return source_skipLine(true);
					}
					error(macroName, "Expected identifier, not " + macroName);
				}
				return source_skipLine(false);
			case "endif":
				pop_state();
				return source_skipLine(warnings.contains(Warning.ENDIF_LABELS));
			case "line":
				return source_skipLine(false);
			case "pragma":
				return isActive() ? parsePragmaDirective() : source_skipLine(false);
			default:
				error(token, "Unknown preprocessor directive " + token);
				return source_skipLine(false);
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
			Token tok;
			if (!isActive()) {
				Source s = getSource();
				if (s == null) {
					Token t = next_source();
					if (t.getType() == P_LINE && !getFeature(Feature.LINEMARKERS))
						continue;
					return t;
				}

				try {
					/* XXX Tell lexer to ignore warnings. */
					s.setActive(false);
					tok = source_token();
				} finally {
					/* XXX Tell lexer to stop ignoring warnings. */
					s.setActive(true);
				}
				switch (tok.getType()) {
					case HASH:
					case NL:
					case EOF:
						/* The preprocessor has to take action here. */
						break;
					case WHITESPACE:
						return tok;
					case C_COMMENT:
					case CPP_COMMENT:
						// Patch up to preserve whitespace.
						if (getFeature(Feature.KEEPALLCOMMENTS))
							return tok;
						if (!isActive())
							return toWhitespace(tok);
						if (getFeature(Feature.KEEPCOMMENTS))
							return tok;
						return toWhitespace(tok);
					default:
						// Return NL to preserve whitespace.
						/* XXX This might lose a comment. */
						return source_skipLine(false);
				}
			} else {
				tok = source_token();
			}

			int type = tok.getType();
			if (type == IDENTIFIER) {
				final Macro macro = getMacro(tok.getText());
				if (macro == null || source.isExpanding(macro)) return tok;
				if (macro(macro, tok)) continue;
				return tok;
			} else if (type == P_LINE) {
				if (getFeature(Feature.LINEMARKERS)) return tok;
			} else if (type == INVALID) {
				error(tok, String.valueOf(tok.getValue()));
				return tok;
			} else if (type == HASH) {
				final Token nextToken = parseDirective();
				if (nextToken != null) return nextToken;
			} else {
				return tok;
			}
		}
	}

	@Nonnull
	private Token token_nonwhite() throws IOException, LexerException {
		Token tok;
		do {
			tok = token();
		} while (isWhite(tok));
		return tok;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();

		Source s = getSource();
		while (s != null) {
			buf.append(" -> ").append(s).append("\n");
			s = s.getParent();
		}

		Map<String, Macro> macros = new TreeMap<>(getMacros());
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

}
