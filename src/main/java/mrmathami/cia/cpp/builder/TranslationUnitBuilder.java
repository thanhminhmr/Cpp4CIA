package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.CppException;
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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class TranslationUnitBuilder {
	@Nonnull static final String VIRTUAL_FILENAME = "##ROOT##";

	@Nonnull private static final IncludeFileContentProvider EMPTY_PROVIDER = IncludeFileContentProvider.getEmptyFilesProvider();
	@Nonnull private static final IParserLogService LOG_SERVICE = new DefaultLogService();
	@Nonnull private static final GPPLanguage GPP_LANGUAGE = GPPLanguage.getDefault();
	@Nonnull private static final IScannerInfo SCANNER_INFO = new ScannerInfo();

	private TranslationUnitBuilder() {
	}

	@Nonnull
	private static Set<Path> createFileIncludes(@Nonnull IASTTranslationUnit translationUnit,
			@Nonnull List<Path> projectFiles, @Nonnull List<Path> internalIncludePaths, @Nonnull Path currentFolder) {
		final Set<Path> includeSet = new HashSet<>();
		final Set<Path> projectFileSet = Set.copyOf(projectFiles);
		for (final IASTPreprocessorIncludeStatement includeDirective : translationUnit.getIncludeDirectives()) {
			if (includeDirective.isActive()) {
				final String includeFileName = includeDirective.getName().toString();
				if (!includeDirective.isSystemInclude()) {
					final Path includeFile = currentFolder.resolve(includeFileName).toAbsolutePath();
					if (projectFileSet.contains(includeFile) && includeSet.add(includeFile)) {
						continue;
					}
				}
				for (final Path includePath : internalIncludePaths) {
					final Path includeFile = includePath.resolve(includeFileName).toAbsolutePath();
					if (projectFileSet.contains(includeFile) && includeSet.add(includeFile)) {
						break;
					}
				}
			}
		}
		return includeSet;
	}

	@Nonnull
	static Map<Path, Set<Path>> createIncludeMap(@Nonnull List<Path> projectFiles,
			@Nonnull List<Path> internalIncludePaths) throws CppException {
		final Map<Path, Set<Path>> includeMap = new HashMap<>();
		for (final Path currentFile : projectFiles) {
			try {
				final IASTTranslationUnit translationUnit = GPP_LANGUAGE.getASTTranslationUnit(
						FileContent.create(currentFile.toString(),
								Files.readString(currentFile, StandardCharsets.UTF_8).toCharArray()),
						SCANNER_INFO, EMPTY_PROVIDER, null,
						ILanguage.OPTION_NO_IMAGE_LOCATIONS
								| ILanguage.OPTION_SKIP_FUNCTION_BODIES
								| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS,
						LOG_SERVICE);

				includeMap.put(currentFile, createFileIncludes(translationUnit, projectFiles, internalIncludePaths, currentFile.getParent()));
			} catch (CoreException e) {
				throw new CppException("Cannot create TranslationUnit!", e);
			} catch (IOException e) {
				throw new CppException("Cannot read project file!", e);
			}
		}
		return includeMap;
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
