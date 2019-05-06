package cia.cpp.builder;

import cia.cpp.ProjectVersion;
import cia.cpp.ast.IRoot;
import cia.cpp.preprocessor.PreprocessorBuilder;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public final class VersionBuilder {
	private VersionBuilder() {
	}

	private static List<Path> createPathList(List<Path> pathList) throws IOException {
		final List<Path> includePaths = new ArrayList<>();
		final Set<Path> includePathSet = new HashSet<>();
		for (final Path path : pathList) {
			final Path realPath = path.toRealPath();
			if (includePathSet.add(realPath)) {
				includePaths.add(realPath);
			}
		}
		return includePaths;
	}

	private static List<Path> createInternalIncludePaths(List<Path> projectFiles) {
		final List<Path> includePaths = new ArrayList<>();
		final Set<Path> includePathSet = new HashSet<>();
		for (final Path projectFile : projectFiles) {
			final Path canonicalAbsoluteFile = projectFile.getParent();
			if (includePathSet.add(canonicalAbsoluteFile)) {
				includePaths.add(canonicalAbsoluteFile);
			}
		}
		return includePaths;
	}

	private static List<Path> combinePathList(List<Path> pathListA, List<Path> pathListB) {
		final List<Path> newList = new ArrayList<>();
		final Set<Path> newSet = new HashSet<>();
		for (final Path path : pathListA) if (newSet.add(path)) newList.add(path);
		for (final Path path : pathListB) if (newSet.add(path)) newList.add(path);
		return newList;
	}

	public static ProjectVersion build(String versionName, Path projectRoot, List<Path> projectFiles, List<Path> includePaths, VersionBuilderDebugger debugger) {
		try {
			final List<Path> projectFileList = createPathList(projectFiles);
			final List<Path> externalIncludePaths = createPathList(includePaths);
			final List<Path> internalIncludePaths = createInternalIncludePaths(projectFileList);
			final List<Path> includePathList = combinePathList(externalIncludePaths, internalIncludePaths);

			if (debugger != null) debugger.setVersionName(versionName);

			final char[] fileContentCharArray = PreprocessorBuilder.build(projectFileList, includePathList, debugger != null && debugger.isReadable());
			if (fileContentCharArray == null) return null;

			if (debugger != null && debugger.isSaveFileContent()) debugger.setFileContent(fileContentCharArray);

			final IASTTranslationUnit translationUnit = TranslationUnitBuilder.build(fileContentCharArray);
			if (translationUnit == null) return null;

			if (debugger != null && debugger.isSaveTranslationUnit()) debugger.setTranslationUnit(translationUnit);

			final IRoot root = AstBuilder.build(translationUnit);

			if (debugger != null && debugger.isSaveRoot()) debugger.setRoot(root);

			final Path projectRootPath = projectRoot.toRealPath();
			final List<String> projectFilePaths = new ArrayList<>();
			for (final Path path : projectFileList) {
				projectFilePaths.add(projectRootPath.relativize(path).toString());
			}
			final List<String> projectIncludePaths = new ArrayList<>();
			for (final Path path : externalIncludePaths) {
				projectIncludePaths.add(projectRootPath.relativize(path).toString());
			}

			return ProjectVersion.of(versionName, projectFilePaths, projectIncludePaths, root);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
