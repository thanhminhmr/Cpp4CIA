package mrmathami.cia.cpp.builder;

import mrmathami.annotations.Nonnull;
import mrmathami.cia.cpp.CppException;
import mrmathami.utils.Pair;
import org.anarres.cpp.InputLexerSource;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.PreprocessorListener;
import org.anarres.cpp.Source;
import org.anarres.cpp.Token;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class PreprocessorBuilder {
	@Nonnull private static final PreprocessorListener EMPTY_PREPROCESSOR_LISTENER = new PreprocessorListener() {
		@Override
		public void handleWarning(@Nonnull Source source, int line, int column, @Nonnull String msg) {
//		    System.out.println(source.getName() + ":" + line + ":" + column + ": warning: " + msg);
		}

		@Override
		public void handleError(@Nonnull Source source, int line, int column, @Nonnull String msg) {
//		    System.out.println(source.getName() + ":" + line + ":" + column + ": error: " + msg);
		}

		@Override
		public void handleSourceChange(@Nonnull Source source, @Nonnull SourceChangeEvent event) {
		}
	};
	@Nonnull private static final List<Preprocessor.Feature> FEATURE_LIST = List.of(
			Preprocessor.Feature.DIGRAPHS,
			Preprocessor.Feature.TRIGRAPHS,
			Preprocessor.Feature.LINEMARKERS,
			Preprocessor.Feature.PRAGMA_ONCE
	);

	private PreprocessorBuilder() {
	}

	@Nonnull
	private static List<Path> includeList(@Nonnull List<Path> projectFiles,
			@Nonnull List<Path> includePaths) throws CppException {

		final List<Pair<Path, Set<Path>>> fileIncludesList
				= TranslationUnitBuilder.createFileIncludesList(projectFiles, includePaths);
		final int includeSize = fileIncludesList.size();
		final List<Path> includeList = new ArrayList<>(includeSize);

		final List<IntObjectPair<Path>> includeCounts = new ArrayList<>(includeSize);
		final Set<Path> includedPaths = new HashSet<>(includeSize);
		final List<Path> minimumCountPaths = new ArrayList<>(includeSize);

		while (includedPaths.size() < includeSize) {
			includeCounts.clear();
			for (final Pair<Path, Set<Path>> entry : fileIncludesList) {
				final Path fromPath = entry.getA();
				if (!includedPaths.contains(fromPath)) {
					int count = 0;
					for (final Path toPath : entry.getB()) {
						if (!includedPaths.contains(toPath)) count++;
					}
					includeCounts.add(new IntObjectPair<>(fromPath, count));
				}
			}

			minimumCountPaths.clear();
			int minCount = Integer.MAX_VALUE;
			for (final IntObjectPair<Path> pair : includeCounts) {
				if (pair.value <= minCount) {
					if (pair.value < minCount) {
						minCount = pair.value;
						minimumCountPaths.clear();
					}
					minimumCountPaths.add(pair.object);
				}
			}

			includedPaths.addAll(minimumCountPaths);
			includeList.addAll(minimumCountPaths);
		}

		return includeList;
	}

	@Nonnull
	public static char[] build(@Nonnull Path projectRootPath, @Nonnull List<Path> projectFiles,
			@Nonnull List<Path> includePaths, boolean isReadable) throws CppException {
		try {
			final Preprocessor preprocessor = new Preprocessor(EMPTY_PREPROCESSOR_LISTENER);
			preprocessor.addFeatures(FEATURE_LIST);
			preprocessor.setSystemIncludePath(includePaths);
			final StringBuilder builder = new StringBuilder();
			for (final Path sourceFile : includeList(projectFiles, includePaths)) {
				builder.append("#include \"").append(projectRootPath.relativize(sourceFile)).append("\"\n");
			}
			final Path virtualFile = projectRootPath.resolve(UUID.randomUUID() + ".virtual_file");
			preprocessor.addInput(new InputLexerSource(new StringReader(builder.toString()), virtualFile));

			// =====
			final StringBuilder fileContent = new StringBuilder();

			if (isReadable) {
				readablePreprocessor(preprocessor, fileContent);
			} else {
				fastPreprocessor(preprocessor, fileContent);
			}
			// =====

			char[] content = new char[fileContent.length()];
			fileContent.getChars(0, content.length, content, 0);
			return content;
		} catch (IOException | LexerException e) {
			throw new CppException("Cannot preprocess the source code!", e);
		}
	}

	private static void fastPreprocessor(@Nonnull Preprocessor preprocessor,
			@Nonnull StringBuilder fileContent) throws IOException, LexerException {
		boolean haveEndSpace = true;
		while (true) {
			final Token token = preprocessor.token();

			switch (token.getType()) {
				case Token.NEW_LINE:
				case Token.WHITESPACE:
				case Token.C_COMMENT:
				case Token.CPP_COMMENT:
				case Token.P_LINE:
					haveEndSpace = true;
					continue;

				case Token.EOF:
					fileContent.append('\n');
					return;
			}
			final String tokenText = token.getText().trim();
			if (tokenText.isBlank()) {
				haveEndSpace = true;
				continue;
			}
			if (haveEndSpace) fileContent.append(' ');
			fileContent.append(tokenText);
			haveEndSpace = false;
		}
	}

	private static void readablePreprocessor(@Nonnull Preprocessor preprocessor,
			@Nonnull StringBuilder fileContent) throws IOException, LexerException {
		int emptyLine = 1;
		final StringBuilder emptyLineBuilder = new StringBuilder();
		while (true) {
			final Token tok = preprocessor.token();
			if (tok.getType() == Token.EOF) break;

			if (tok.getType() != Token.C_COMMENT && tok.getType() != Token.CPP_COMMENT) {
				final String tokText = tok.getText()
						.replace("\r\n", "\n")
						.replace('\r', '\n');
				if (tok.getType() != Token.WHITESPACE && !tokText.isBlank()) {
					if (tok.getType() != Token.P_LINE && emptyLine > 0) {
						fileContent.append(emptyLineBuilder);
					}
					fileContent.append(tokText);
					emptyLineBuilder.setLength(0);
					emptyLine = 0;
				} else {
					if (!tokText.contains("\n")) {
						if (emptyLine == 0) {
							fileContent.append(' ');
						} else {
							emptyLineBuilder.append(tokText);
						}
					} else if (emptyLine < 2) {
						fileContent.append('\n');
						emptyLineBuilder.setLength(0);
						emptyLine += 1;
					} else {
						emptyLineBuilder.setLength(0);
					}
				}
			}
		}
		fileContent.append('\n');
	}

	private static final class IntObjectPair<E> {
		@Nonnull private final E object;
		private final int value;

		private IntObjectPair(@Nonnull E object, int value) {
			this.object = object;
			this.value = value;
		}
	}
}