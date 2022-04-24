package mrmathami.cia.cpp.builder;

import mrmathami.annotations.Nonnull;
import mrmathami.cia.cpp.CppException;
import mrmathami.utils.EncodingDetector;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.runtime.CoreException;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

final class TranslationUnitBuilder {
	@Nonnull static final String VIRTUAL_FILENAME = "##ROOT##";

	@Nonnull private static final IncludeFileContentProvider EMPTY_PROVIDER = IncludeFileContentProvider.getEmptyFilesProvider();
	@Nonnull private static final IParserLogService LOG_SERVICE = new DefaultLogService();
	@Nonnull private static final GPPLanguage GPP_LANGUAGE = GPPLanguage.getDefault();
	@Nonnull private static final IScannerInfo SCANNER_INFO = new ScannerInfo();

	private TranslationUnitBuilder() {
	}

	@Nonnull
	private static String getExtension(@Nonnull Path path) {
		final String file = path.getFileName().toString();
		final int dot = file.lastIndexOf('.');
		return dot >= 0 ? file.substring(dot).toLowerCase(Locale.ROOT) : "";
	}

	@Nonnull
	static Map<Path, Set<Path>> createFileIncludes(@Nonnull List<Path> projectFiles, @Nonnull List<Path> includePaths)
			throws CppException {
		final Map<Path, Set<Path>> includeList = new TreeMap<>(
				Comparator.comparing(TranslationUnitBuilder::getExtension).reversed()
						.thenComparing(Path::compareTo));
		final Map<Path, Path> projectFileMap = new HashMap<>(2 * projectFiles.size());
		for (final Path projectFile : projectFiles) projectFileMap.put(projectFile, projectFile);
		for (final Path projectFile : projectFiles) {
			try (final CharArrayWriter writer = new CharArrayWriter()) {
				try (final Reader reader = EncodingDetector.createReader(Files.newInputStream(projectFile))) {
					reader.transferTo(writer);
				}
				final IASTTranslationUnit translationUnit = GPP_LANGUAGE.getASTTranslationUnit(
						FileContent.create(projectFile.toString(), writer.toCharArray()),
						SCANNER_INFO, EMPTY_PROVIDER, null,
						ILanguage.OPTION_NO_IMAGE_LOCATIONS
								| ILanguage.OPTION_SKIP_FUNCTION_BODIES
								| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS,
						LOG_SERVICE);

				final Path currentFolder = projectFile.getParent();
				final Set<Path> includeSet = new LinkedHashSet<>();
				for (final IASTPreprocessorIncludeStatement includeDirective : translationUnit.getIncludeDirectives()) {
					final String includeFileName = includeDirective.getName().toString();
					if (!includeDirective.isSystemInclude()) {
						final Path includeFile = currentFolder.resolve(includeFileName).normalize();
						final Path normalizedIncludeFile = projectFileMap.get(includeFile);
						if (normalizedIncludeFile != null) {
							includeSet.add(normalizedIncludeFile);
							continue;
						}
					}
					for (final Path includePath : includePaths) {
						final Path includeFile = includePath.resolve(includeFileName).normalize();
						final Path normalizedIncludeFile = projectFileMap.get(includeFile);
						if (normalizedIncludeFile != null) {
							includeSet.add(normalizedIncludeFile);
							break;
						}
					}
				}
				includeList.put(projectFile, includeSet);
			} catch (CoreException e) {
				throw new CppException("Cannot create TranslationUnit!", e);
			} catch (IOException e) {
				throw new CppException("Cannot read project file!", e);
			}
		}
		return includeList;
	}

	@Nonnull
	static IASTTranslationUnit build(@Nonnull char[] fileContentChars) throws CppException {
		final FileContent fileContent = FileContent.create(VIRTUAL_FILENAME, fileContentChars);
		try {
			return GPP_LANGUAGE.getASTTranslationUnit(fileContent, SCANNER_INFO, EMPTY_PROVIDER, null,
					ILanguage.OPTION_NO_IMAGE_LOCATIONS
							| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS,
					LOG_SERVICE);
		} catch (CoreException e) {
			throw new CppException("Cannot create TranslationUnit!", e);
		}
	}
}
