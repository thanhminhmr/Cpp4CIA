package cia.cpp.builder;

import mrmathami.util.Utilities;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIncludeStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.core.runtime.CoreException;

import java.io.File;
import java.util.*;

final class TranslationUnitBuilder {
	private static final IncludeFileContentProvider EMPTY_PROVIDER = IncludeFileContentProvider.getEmptyFilesProvider();
	private static final IParserLogService LOG_SERVICE = new DefaultLogService();
	private static final GPPLanguage GPP_LANGUAGE = GPPLanguage.getDefault();
	private static final IScannerInfo SCANNER_INFO = new ScannerInfo();

	private TranslationUnitBuilder() {
	}

	private static List<File> createFileIncludes(IASTTranslationUnit translationUnit, List<File> projectFiles, List<File> internalIncludePaths, File currentFolder) {
		final List<File> includeList = new ArrayList<>();
		final Set<File> includeSet = new HashSet<>();
		final Set<File> projectFileSet = Set.copyOf(projectFiles);
		for (final IASTPreprocessorIncludeStatement includeDirective : translationUnit.getIncludeDirectives()) {
			if (includeDirective.isActive()) {
				final String includeFileName = includeDirective.getName().toString();
				{
					final File includeFile = Utilities.getCanonicalAbsoluteFile(new File(currentFolder, includeFileName));
					if (projectFileSet.contains(includeFile) && includeSet.add(includeFile)) {
						includeList.add(includeFile);
						continue;
					}
				}
				for (final File includePath : internalIncludePaths) {
					final File includeFile = Utilities.getCanonicalAbsoluteFile(new File(includePath, includeFileName));
					if (projectFileSet.contains(includeFile) && includeSet.add(includeFile)) {
						includeList.add(includeFile);
						break;
					}
				}
			}
		}
		return includeList;
	}

	public static Map<File, List<File>> createIncludeMap(List<File> projectFiles, List<File> internalIncludePaths) {
		final Map<File, List<File>> includeMap = new HashMap<>();
		for (final File currentFile : projectFiles) {
			try {
				final IASTTranslationUnit translationUnit = GPP_LANGUAGE.getASTTranslationUnit(
						FileContent.create(currentFile.getName(), Utilities.readFile(currentFile)),
						SCANNER_INFO, EMPTY_PROVIDER, null,
						ILanguage.OPTION_NO_IMAGE_LOCATIONS
								| ILanguage.OPTION_SKIP_FUNCTION_BODIES
								| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS,
						LOG_SERVICE);

				includeMap.put(currentFile, createFileIncludes(translationUnit, projectFiles, internalIncludePaths, currentFile.getParentFile()));
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
