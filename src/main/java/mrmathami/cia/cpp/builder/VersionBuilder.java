package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.ast.RootNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VersionBuilder {
	@Nonnull public static final Map<DependencyType, Double> WEIGHT_MAP = Map.of(
			DependencyType.USE, 4.0,
			DependencyType.MEMBER, 3.0,
			DependencyType.INHERITANCE, 4.0,
			DependencyType.INVOCATION, 3.5,
			DependencyType.OVERRIDE, 3.3
	);

	private VersionBuilder() {
	}

	@Nonnull
	private static List<Path> createPathList(@Nonnull List<Path> pathList) {
		final List<Path> paths = new ArrayList<>();
		final Set<Path> pathSet = new HashSet<>();
		for (final Path path : pathList) {
			final Path realPath = path.toAbsolutePath();
			if (pathSet.add(realPath)) {
				paths.add(realPath);
			}
		}
		return paths;
	}

	@Nonnull
	private static List<String> createRelativePathStrings(@Nonnull List<Path> pathList, Path rootPath) {
		final List<String> pathStrings = new ArrayList<>();
		for (final Path path : pathList) {
			pathStrings.add(rootPath.relativize(path).toString());
		}
		return pathStrings;
	}

	@Nonnull
	private static List<Path> createInternalIncludePaths(@Nonnull List<Path> projectFiles) {
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

	@Nonnull
	private static List<Path> combinePathList(@Nonnull List<Path> pathListA, List<Path> pathListB) {
		final List<Path> newList = new ArrayList<>();
		final Set<Path> newSet = new HashSet<>();
		for (final Path path : pathListA) if (newSet.add(path)) newList.add(path);
		for (final Path path : pathListB) if (newSet.add(path)) newList.add(path);
		return newList;
	}

	@Nonnull
	private static double[] calculateWeights(@Nonnull double[] weightMap, @Nonnull RootNode rootNode) {
		final double[] weights = new double[rootNode.getNodeCount()];
		for (final CppNode node : rootNode) {
			double directWeight = 0.0;
			for (final CppNode dependencyNode : node.getAllDependencyFrom()) {
				for (final Map.Entry<DependencyType, Integer> entry : node.getNodeDependencyFrom(dependencyNode).entrySet()) {
					directWeight += weightMap[entry.getKey().ordinal()] * entry.getValue();
				}
			}
			weights[node.getId()] = directWeight;
		}
		return weights;
	}

	@Nonnull
	public static ProjectVersion build(@Nonnull String versionName, @Nonnull Path projectRoot,
			@Nonnull List<Path> projectFiles, @Nonnull List<Path> includePaths,
			@Nonnull Map<DependencyType, Double> dependencyTypeWeightMap) throws CppException {
		final List<Path> projectFileList = createPathList(projectFiles);
		final List<Path> externalIncludePaths = createPathList(includePaths);
		final List<Path> internalIncludePaths = createInternalIncludePaths(projectFileList);
		final List<Path> includePathList = combinePathList(externalIncludePaths, internalIncludePaths);

		final char[] fileContentCharArray = PreprocessorBuilder.build(projectFileList, includePathList, false);
		final IASTTranslationUnit translationUnit = TranslationUnitBuilder.build(fileContentCharArray);
		final RootNode root = AstBuilder.build(translationUnit);

		final Path projectRootPath = projectRoot.toAbsolutePath();
		final List<String> projectFilePaths = createRelativePathStrings(projectFileList, projectRootPath);
		final List<String> projectIncludePaths = createRelativePathStrings(externalIncludePaths, projectRootPath);

		final DependencyType[] types = DependencyType.values();
		final double[] typeWeights = new double[types.length];
		for (final DependencyType type : types) typeWeights[type.ordinal()] = dependencyTypeWeightMap.get(type);

		final double[] weights = calculateWeights(typeWeights, root);
		return new ProjectVersion(versionName, projectFilePaths, projectIncludePaths, root, typeWeights, weights);
	}

	//*
	@Nonnull
	public static ProjectVersion build(@Nonnull String versionName, @Nonnull Path projectRoot,
			@Nonnull List<Path> projectFiles, @Nonnull List<Path> includePaths,
			@Nonnull Map<DependencyType, Double> dependencyTypeWeightMap, @Nonnull VersionBuilderDebugger debugger)
			throws CppException {
		final List<Path> projectFileList = createPathList(projectFiles);
		final List<Path> externalIncludePaths = createPathList(includePaths);
		final List<Path> internalIncludePaths = createInternalIncludePaths(projectFileList);
		final List<Path> includePathList = combinePathList(externalIncludePaths, internalIncludePaths);

		debugger.setVersionName(versionName);

		final char[] fileContentCharArray = debugger.loadFileContent()
				? debugger.getFileContent()
				: PreprocessorBuilder.build(projectFileList, includePathList, debugger.isReadableFileContent());

		debugger.saveFileContent(fileContentCharArray);

		final IASTTranslationUnit translationUnit = TranslationUnitBuilder.build(fileContentCharArray);

		if (debugger.isSaveTranslationUnit()) debugger.saveTranslationUnit(translationUnit);

		final RootNode root = AstBuilder.build(translationUnit);

		debugger.saveRoot(root);

		final Path projectRootPath = projectRoot.toAbsolutePath();
		final List<String> projectFilePaths = createRelativePathStrings(projectFileList, projectRootPath);
		final List<String> projectIncludePaths = createRelativePathStrings(externalIncludePaths, projectRootPath);

		final DependencyType[] types = DependencyType.values();
		final double[] typeWeights = new double[types.length];
		for (final DependencyType type : types) typeWeights[type.ordinal()] = dependencyTypeWeightMap.get(type);

		final double[] weights = calculateWeights(typeWeights, root);
		return new ProjectVersion(versionName, projectFilePaths, projectIncludePaths, root, typeWeights, weights);
	}
	//*/
}
