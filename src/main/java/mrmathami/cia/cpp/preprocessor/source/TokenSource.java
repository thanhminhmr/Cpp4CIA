package mrmathami.cia.cpp.preprocessor.source;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import mrmathami.cia.cpp.preprocessor.EventListener;
import mrmathami.cia.cpp.preprocessor.codepoint.CodepointReader;
import mrmathami.cia.cpp.preprocessor.token.Token;
import mrmathami.cia.cpp.preprocessor.token.TokenType;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TokenSource implements Serializable {
	private static final long serialVersionUID = -1L;

	@Nonnull
	public static TokenSource fromReader(@Nonnull Reader reader, @Nonnull EventListener listener) throws IOException {
		final CodepointReader codepointReader = CodepointReader.fromReader(reader);
		final List<Token> tokens = new ArrayList<>();
		final TokenSource tokenSource = new TokenSource(tokens);
		final SourceLexer lexer = new SourceLexer(codepointReader, listener, tokenSource);
		boolean newLine = false;
		boolean directive = false;
		boolean header = false;
		while (true) {
			final Token token = lexer.nextToken();
			final TokenType type = token.getType();
			if (type == TokenType.EOF) break;
			tokens.add(token);
			if (header && type != TokenType.WHITESPACE) {
				header = false;
				lexer.expectHeaderName(false);
			}
			if (newLine && (type == TokenType.HASH || type == TokenType.HASH_ALT)) {
				// preprocessor directive
				newLine = false;
				directive = true;
			} else if (directive && type == TokenType.IDENTIFIER && token.getText().equals("include")) {
				header = true;
				lexer.expectHeaderName(true);
			} else if (type == TokenType.NEW_LINE) {
				newLine = true;
				directive = false;
			} else if (type != TokenType.WHITESPACE) {
				newLine = false;
				directive = false;
			}
		}
		return tokenSource;
	}

	@Nonnull
	public static TokenSource fromPaste(@Nonnull String string, @Nonnull EventListener listener) throws IOException {
		final CodepointReader codepointReader = CodepointReader.fromCodepoints(string.codePoints().toArray());
		final List<Token> tokens = new ArrayList<>();
		final TokenSource tokenSource = new TokenSource(tokens);
		final SourceLexer lexer = new SourceLexer(codepointReader, listener, tokenSource);
		while (true) {
			final Token token = lexer.nextToken();
			if (token.getType() == TokenType.EOF) break;
			tokens.add(token);
		}
		if (tokens.isEmpty()) {
			listener.handleWarning(tokenSource, 1, 1, "Paste is empty!");
			tokens.add(new Token(TokenType.WHITESPACE, " ", tokenSource, 1, 1, 1, 1));
		} else if (tokens.size() > 1) {
			listener.handleError(tokenSource, 0, 0, "Paste create multiple tokens");
		}
		return tokenSource;
	}

	// =====

	@Nonnull private final List<Token> tokens;

	@Nonnull private String name = "Unknown";
	@Nullable private Path path;
	@Nullable private TokenSource parent;

	private TokenSource(@Nonnull List<Token> tokens) {
		this.tokens = tokens;
	}

	@Nonnull
	public List<Token> getTokens() {
		return Collections.unmodifiableList(tokens);
	}

	@Nonnull
	public String getName() {
		return name;
	}

	public void setName(@Nonnull String name) {
		this.name = name;
	}

	@Nullable
	public Path getPath() {
		return path;
	}

	public void setPath(@Nullable Path path) {
		this.path = path;
	}

	@Nullable
	public TokenSource getParent() {
		return parent;
	}

	public void setParent(@Nullable TokenSource parent) {
		this.parent = parent;
	}
}
