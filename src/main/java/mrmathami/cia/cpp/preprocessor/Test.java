package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Test {
	private Test() {
	}

	public static void main(String[] strings) throws IOException, PreprocessorException {
		/*
		final BufferedReader bufferedReader = Files.newBufferedReader(
				Path.of("test/gcc-7.5.0/gcc/testsuite/c-c++-common/cpp/normalize-3.c")
		);
		final LexerReader reader = new LexerReader(bufferedReader);

		while (true) {
			final int cp = reader.read();
			if (cp < 0) break;
			System.out.print(Character.toChars(cp));
		}
		System.out.println();
		/*/
		final BufferedReader bufferedReader = Files.newBufferedReader(
				Path.of("test/gcc-7.5.0/gcc/testsuite/c-c++-common/cpp/pr58844-1.c")
		);
		final LexerReader reader = new LexerReader(bufferedReader);
		final AbstractLexerSource source = new AbstractLexerSource(reader, new EventListener() {
			@Override
			public void handleWarning(@Nonnull TokenSource source, int line, int column, @Nonnull String msg) throws PreprocessorException {
			}

			@Override
			public void handleError(@Nonnull TokenSource source, int line, int column, @Nonnull String msg) throws PreprocessorException {
			}

			@Override
			public void handleCritical(@Nonnull TokenSource source, int line, int column, @Nonnull String msg) throws PreprocessorException {
			}

			@Override
			public void handleSourceChange(@Nonnull TokenSource source, @Nonnull SourceChangeEvent event) {
			}
		}) {
			@Nonnull
			@Override
			public String getName() {
				return "null";
			}

			@Nullable
			@Override
			public Path getPath() {
				return null;
			}
		};

		while (true) {
			final Token token = source.nextToken();
			if (token.getType() == TokenType.EOF) break;
			System.out.println(token.getText());
		}
		//*/
	}
}
