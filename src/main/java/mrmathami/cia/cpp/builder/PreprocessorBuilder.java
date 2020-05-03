package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.CppException;
import org.anarres.cpp.Feature;
import org.anarres.cpp.LexerException;
import org.anarres.cpp.Preprocessor;
import org.anarres.cpp.PreprocessorListener;
import org.anarres.cpp.Source;
import org.anarres.cpp.Token;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

	private PreprocessorBuilder() {
	}

	@Nonnull
	private static String getFileExtension(@Nonnull Path file) {
		final String filename = file.getFileName().toString();
		final int dot = filename.lastIndexOf('.');
		return dot >= 0 ? filename.substring(dot) : "";
	}

	private static int fileCompare(@Nonnull Path fileA, @Nonnull Path fileB) {
		final int compare = getFileExtension(fileA).compareToIgnoreCase(getFileExtension(fileB));
		if (compare != 0) return compare;
		return fileA.toString().compareToIgnoreCase(fileB.toString());
	}

	@Nonnull
	private static List<Path> includeList(@Nonnull List<Path> projectFiles,
			@Nonnull List<Path> includePaths) throws CppException {
		final Map<Path, Set<Path>> includeMap = TranslationUnitBuilder.createIncludeMap(projectFiles, includePaths);

		final Map<Path, Integer> includeScoreMap = new HashMap<>();
		final List<Path> includeList = new ArrayList<>(includeMap.size());

		final List<Path> minIncludeFiles = new ArrayList<>();

		while (!includeMap.isEmpty()) {
			long minCount = Long.MAX_VALUE;

			includeScoreMap.clear();
			for (final Map.Entry<Path, Set<Path>> entry : includeMap.entrySet()) {
				includeScoreMap.put(entry.getKey(), 0);
				for (final Path sourceIncludePath : entry.getValue()) {
					if (includeMap.containsKey(sourceIncludePath)) {
						final Integer count = includeScoreMap.get(sourceIncludePath);
						includeScoreMap.put(sourceIncludePath, count != null ? count + 1 : 1);
					}
				}
			}

			minIncludeFiles.clear();
			for (final Map.Entry<Path, Integer> entry : includeScoreMap.entrySet()) {
				if (entry.getValue() <= minCount) {
					if (entry.getValue() < minCount) {
						minCount = entry.getValue();
						minIncludeFiles.clear();
					}
					minIncludeFiles.add(entry.getKey());
				}
			}

			minIncludeFiles.sort(PreprocessorBuilder::fileCompare);
			for (final Path includeFile : minIncludeFiles) includeMap.remove(includeFile);
			includeList.addAll(minIncludeFiles);
		}

		return includeList;
	}

	@Nonnull
	public static char[] build(@Nonnull List<Path> projectFiles,
			@Nonnull List<Path> includePaths, boolean isReadable) throws CppException {
		try {
			final Preprocessor preprocessor = new Preprocessor();
			preprocessor.setListener(EMPTY_PREPROCESSOR_LISTENER);
			preprocessor.addFeatures(Feature.DIGRAPHS, Feature.TRIGRAPHS, Feature.INCLUDENEXT, Feature.PRAGMA_ONCE, Feature.LINEMARKERS);

			{
				final List<String> includePathStrings = new ArrayList<>();
				for (final Path path : includeList(projectFiles, includePaths)) {
					includePathStrings.add(path.toAbsolutePath().toString());
				}
				preprocessor.setQuoteIncludePath(includePathStrings);
				preprocessor.setSystemIncludePath(includePathStrings);

				final List<Path> projectFileList = new ArrayList<>();
				for (final Path projectFile : projectFiles) {
					projectFileList.add(projectFile.toAbsolutePath());
				}
				//projectFileList.sort(PreprocessorBuilder::fileCompare);

				for (final Path sourceFile : projectFileList) {
					preprocessor.addInput(sourceFile.toFile());
				}
			}
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
				case Token.NL:
				case Token.WHITESPACE:
				case Token.CCOMMENT:
				case Token.CPPCOMMENT:
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

			if (tok.getType() != Token.CCOMMENT && tok.getType() != Token.CPPCOMMENT) {
				final String tokText = tok.getText().replace("\r\n", "\n").replace('\r', '\n');
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
}