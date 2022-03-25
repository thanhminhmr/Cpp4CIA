package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;
import mrmathami.cia.cpp.preprocessor.source.TokenSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Test {
	private Test() {
	}

	public static void main(String[] strings) throws IOException {
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
		final EventListener eventListener = new EventListener() {
			@Override
			public void handleWarning(@Nonnull TokenSource source, int line, int column, @Nonnull String msg) throws PreprocessorException {
			}

			@Override
			public void handleError(@Nonnull TokenSource source, int line, int column, @Nonnull String msg) throws PreprocessorException {
			}

			@Override
			public void handleCritical(@Nonnull TokenSource source, int line, int column, @Nonnull String msg) throws PreprocessorException {
			}
		};
		try (final BufferedReader reader = Files.newBufferedReader(Path.of("./local/fv.cpp"))) {
			final TokenSource tokens = TokenSource.fromReader(reader, eventListener);
			System.out.println(tokens);
		}
		//*/
	}
}
