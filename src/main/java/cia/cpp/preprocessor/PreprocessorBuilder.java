package cia.cpp.preprocessor;


import org.anarres.cpp.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class PreprocessorBuilder implements PreprocessorListener {
	private static final PreprocessorBuilder EMPTY_PREPROCESSOR_LISTENER = new PreprocessorBuilder();

	private PreprocessorBuilder() {
	}

	private static String getFileExtension(Path file) {
		final String filename = file.getFileName().toString();
		final int dot = filename.lastIndexOf('.');
		return dot >= 0 ? filename.substring(dot) : "";
	}

	private static int fileCompare(Path fileA, Path fileB) {
		final int compare = getFileExtension(fileA).compareToIgnoreCase(getFileExtension(fileB));
		if (compare != 0) return compare;
		return fileA.toString().compareToIgnoreCase(fileB.toString());
	}

	public static char[] build(List<Path> projectFiles, List<Path> includePaths, boolean isReadable) {
		try {
			final Preprocessor preprocessor = new Preprocessor();
			preprocessor.setListener(EMPTY_PREPROCESSOR_LISTENER);
			preprocessor.addFeatures(Feature.DIGRAPHS, Feature.TRIGRAPHS, Feature.INCLUDENEXT, Feature.PRAGMA_ONCE, Feature.LINEMARKERS);

			{
				final List<String> includePathStrings = new ArrayList<>();
				for (final Path file : includePaths) {
					includePathStrings.add(file.toRealPath().toString());
				}
				preprocessor.setQuoteIncludePath(includePathStrings);
				preprocessor.setSystemIncludePath(includePathStrings);

				final List<Path> projectFileList = new ArrayList<>();
				for (final Path projectFile : projectFiles) {
					projectFileList.add(projectFile.toRealPath());
				}
				projectFileList.sort(PreprocessorBuilder::fileCompare);

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
			e.printStackTrace();
		}
		return null;
	}

	private static void fastPreprocessor(Preprocessor preprocessor, StringBuilder fileContent) throws IOException, LexerException {
		boolean haveEndSpace = true;
		while (true) {
			final Token token = preprocessor.token();

			switch (token.getType()) {
				case Token.NL:
				case Token.WHITESPACE:
					haveEndSpace = true;

				case Token.CCOMMENT:
				case Token.CPPCOMMENT:
				case Token.P_LINE:
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

	private static void readablePreprocessor(Preprocessor preprocessor, StringBuilder fileContent) throws IOException, LexerException {
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

	@Override
	public void handleWarning(@Nonnull Source source, int line, int column, @Nonnull String msg) {
//		System.out.println(source.getName() + ":" + line + ":" + column + ": warning: " + msg);
	}

	@Override
	public void handleError(@Nonnull Source source, int line, int column, @Nonnull String msg) {
//		System.out.println(source.getName() + ":" + line + ":" + column + ": error: " + msg);
	}

	@Override
	public void handleSourceChange(@Nonnull Source source, @Nonnull SourceChangeEvent event) {
	}
}