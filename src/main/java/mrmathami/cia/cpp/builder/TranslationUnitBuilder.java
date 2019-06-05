package mrmathami.cia.cpp.builder;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.core.runtime.CoreException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class TranslationUnitBuilder {
	private static final IncludeFileContentProvider EMPTY_PROVIDER = IncludeFileContentProvider.getEmptyFilesProvider();
	private static final IParserLogService LOG_SERVICE = new DefaultLogService();
	private static final GPPLanguage GPP_LANGUAGE = GPPLanguage.getDefault();
	private static final IScannerInfo SCANNER_INFO = new ScannerInfo();

	private TranslationUnitBuilder() {
	}

	private static Set<Path> createFileIncludes(IASTTranslationUnit translationUnit, List<Path> projectFiles, List<Path> internalIncludePaths, Path currentFolder) {
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

	public static Map<Path, Set<Path>> createIncludeMap(List<Path> projectFiles, List<Path> internalIncludePaths) throws IOException {
		final Map<Path, Set<Path>> includeMap = new HashMap<>();
		for (final Path currentFile : projectFiles) {
			try {
				final IASTTranslationUnit translationUnit = GPP_LANGUAGE.getASTTranslationUnit(
						FileContent.create(currentFile.toString(), Files.readString(currentFile).toCharArray()),
						SCANNER_INFO, EMPTY_PROVIDER, null,
						ILanguage.OPTION_NO_IMAGE_LOCATIONS
								| ILanguage.OPTION_SKIP_FUNCTION_BODIES
								| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS,
						LOG_SERVICE);

				includeMap.put(currentFile, createFileIncludes(translationUnit, projectFiles, internalIncludePaths, currentFile.getParent()));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return includeMap;
	}

	public static IASTTranslationUnit build(char[] fileContentChars) {
		final FileContent fileContent = FileContent.create("ROOT", fileContentChars);
		try {
			return GPP_LANGUAGE.getASTTranslationUnit(
					fileContent, SCANNER_INFO, EMPTY_PROVIDER, null,
					ILanguage.OPTION_NO_IMAGE_LOCATIONS
							| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS,
					LOG_SERVICE);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}