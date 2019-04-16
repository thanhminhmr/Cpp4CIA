package cia.cpp.builder;

import cia.cpp.ProjectVersion;
import cia.cpp.ast.IRoot;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
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

	private static void _debugPrinter(PrintStream printStream, int level, IASTNode node, IASTTranslationUnit translationUnit) {
		final String raw = node.getRawSignature();
		final int cr = raw.indexOf('\r');
		final int lf = raw.indexOf('\n');
		final int line = (cr < 0 || lf < 0) ? Integer.max(cr, lf) : Integer.min(cr, lf);
		final String rawSub = (line < 0 || line > 60) ? (raw.length() > 60 ? raw.substring(0, 60) : raw) : raw.substring(0, line);

		if (node instanceof IASTName) {
			IBinding iBinding = ((IASTName) node).resolveBinding();
			IASTName[] names = iBinding != null ? translationUnit.getDeclarationsInAST(iBinding) : null;

			printStream.printf("%" + (level != 0 ? (level * 2) : "") + "s%-" + (100 - level * 2 - 13) + "s (0x%08X) + %-60s + %-30s | %-50s | %-50s | %s\n",
					"",
					node.getClass().getSimpleName(), node.hashCode(),
					rawSub,
					node.getFileLocation(),
					iBinding != null ? String.format("(0x%08X) %s", iBinding.hashCode(), iBinding.getClass().getSimpleName()) : null,
					iBinding != null ? iBinding.getName() : null,
					iBinding != null ? Arrays.toString(Arrays.stream(names).map(iastName -> {
						if (iastName == null) return "{ null } ";
						IASTImageLocation location = iastName.getImageLocation();
						if (location == null) return "{ " + iastName.toString() + " } ";
						return "{ " + iastName.toString() + ", " + location.getFileName() + "["
								+ location.getNodeOffset() + ", " + (location.getNodeOffset()
								+ location.getNodeLength()) + "] } ";
					}).toArray()) : null
			);
		} else {
			printStream.printf("%" + (level != 0 ? (level * 2) : "") + "s%-" + (100 - level * 2 - 13) + "s (0x%08X) | %-60s | %s\n",
					"",
					node.getClass().getSimpleName(), node.hashCode(),
					rawSub,
					node.getFileLocation()
			);

		}

		for (IASTNode child : node.getChildren()) {
			_debugPrinter(printStream, level + 1, child, translationUnit);
		}
	}

	public static ProjectVersion build(String versionName, Path projectRoot, List<Path> projectFiles, List<Path> includePaths, boolean isReadable) {
		try {
			final List<Path> projectFileList = createPathList(projectFiles);
			final List<Path> externalIncludePaths = createPathList(includePaths);
			final List<Path> internalIncludePaths = createInternalIncludePaths(projectFileList);
			final List<Path> includePathList = combinePathList(externalIncludePaths, internalIncludePaths);

			final char[] fileContentCharArray = PreprocessorBuilder.build(projectFileList, includePathList, isReadable);
			if (fileContentCharArray == null) return null;

			// todo: dbg
			{
				try (final FileWriter writer = new FileWriter("R:\\output_" + versionName + ".cpp")) {
					writer.write(fileContentCharArray);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			final IASTTranslationUnit translationUnit = TranslationUnitBuilder.build(fileContentCharArray);
			if (translationUnit == null) return null;

			// todo: dbg
//		{
//			try (final FileOutputStream fileOutputStream = new FileOutputStream("R:\\preprocessed_" + versionName + ".log")) {
//				try (final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 65536)) {
//					try (final PrintStream printStream = new PrintStream(bufferedOutputStream, false)) {
//						_debugPrinter(printStream, 0, translationUnit, translationUnit);
//					}
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}

			final IRoot root = AstBuilder.build(translationUnit);

			// todo: dbg
			{
				try (final FileWriter fileWriter = new FileWriter("R:\\tree_new_" + versionName + ".log")) {
					fileWriter.write(root.toTreeString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

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
