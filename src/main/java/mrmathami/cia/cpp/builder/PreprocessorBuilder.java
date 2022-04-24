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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

	@Nonnull private static final Set<String> SOURCE_EXTENSIONS = Set.of(".c", ".cc", ".cpp", ".c++", ".cxx");
	@Nonnull private static final Set<String> HEADER_EXTENSIONS = Set.of(".h", ".hh", ".hpp", ".h++", ".hxx");

	private PreprocessorBuilder() {
	}

	@Nonnull
	private static Collection<Path> parseIncludes(@Nonnull List<Path> projectFiles,
			@Nonnull List<Path> includePaths) throws CppException {

		final Map<Path, Set<Path>> fileIncludes
				= TranslationUnitBuilder.createFileIncludes(projectFiles, includePaths);

		final int size = fileIncludes.size() * 2;
		final Set<Path> unknownIncludes = new LinkedHashSet<>(size);
		final Set<Path> headerIncludes = new LinkedHashSet<>(size);
		final Set<Path> sourceIncludes = new LinkedHashSet<>(size);
		final Set<Path> processingIncludes = new HashSet<>(size);
		final Deque<Pair<Path, Iterator<Path>>> queue = new ArrayDeque<>(size);
		for (final Map.Entry<Path, Set<Path>> entry : fileIncludes.entrySet()) {
			final Path processingInclude = entry.getKey();
			processingIncludes.add(processingInclude);
			queue.push(Pair.immutableOf(processingInclude, entry.getValue().iterator()));
			QUEUE:
			while (!queue.isEmpty()) {
				final Pair<Path, Iterator<Path>> pair = queue.peek();
				final Path filePath = pair.getA();
				final Iterator<Path> iterator = pair.getB();
				while (iterator.hasNext()) {
					final Path nextFile = iterator.next();
					if (unknownIncludes.contains(nextFile)
							|| headerIncludes.contains(nextFile)
							|| sourceIncludes.contains(nextFile)) {
						continue;
					}
					if (processingIncludes.add(nextFile)) {
						queue.push(Pair.immutableOf(nextFile, fileIncludes.get(nextFile).iterator()));
						continue QUEUE;
					}
				}

				final String fileName = filePath.getFileName().toString();
				final int dot = fileName.lastIndexOf('.');
				if (dot >= 0) {
					final String extension = fileName.substring(dot).toLowerCase(Locale.ROOT);
					if (HEADER_EXTENSIONS.contains(extension)) {
						headerIncludes.add(filePath);
					} else if (SOURCE_EXTENSIONS.contains(extension)) {
						sourceIncludes.add(filePath);
					} else {
						unknownIncludes.add(filePath);
					}
				} else {
					unknownIncludes.add(filePath);
				}
				queue.pop();
			}
		}

		final List<Path> includes = new ArrayList<>(headerIncludes);
		includes.addAll(sourceIncludes);
		includes.addAll(unknownIncludes);
		return includes;
	}

	@Nonnull
	public static char[] build(@Nonnull Path projectRootPath, @Nonnull List<Path> projectFiles,
			@Nonnull List<Path> includePaths, boolean isReadable) throws CppException {
		try {
			final Preprocessor preprocessor = new Preprocessor(EMPTY_PREPROCESSOR_LISTENER);
			preprocessor.addFeatures(FEATURE_LIST);
			preprocessor.setSystemIncludePath(includePaths);
			final StringBuilder builder = new StringBuilder();
			for (final Path sourceFile : parseIncludes(projectFiles, includePaths)) {
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
}