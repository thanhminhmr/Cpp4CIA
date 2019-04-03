package cia.cpp.builder;

import cia.cpp.ast.IRoot;
import mrmathami.util.Utilities;
import org.eclipse.cdt.core.dom.ast.*;

import java.io.*;
import java.util.*;

public final class ProjectBuilder {
	private ProjectBuilder() {
	}

	private static List<File> createInternalIncludePaths(List<File> projectFiles) {
		final List<File> includePaths = new ArrayList<>();
		final Set<File> includePathSet = new HashSet<>();
		for (final File projectFile : projectFiles) {
			final File canonicalAbsoluteFile = Utilities.getCanonicalAbsoluteFile(projectFile.getParentFile());
			if (includePathSet.add(canonicalAbsoluteFile)) {
				includePaths.add(canonicalAbsoluteFile);
			}
		}
		return includePaths;
	}

	private static List<File> createCanonicalAbsoluteFileList(List<File> fileList) {
		final List<File> includePaths = new ArrayList<>();
		final Set<File> includePathSet = new HashSet<>();
		for (final File file : fileList) {
			final File canonicalAbsoluteFile = Utilities.getCanonicalAbsoluteFile(file);
			if (includePathSet.add(canonicalAbsoluteFile)) {
				includePaths.add(canonicalAbsoluteFile);
			}
		}
		return includePaths;
	}

	private static List<File> combineCanonicalAbsoluteFileList(List<File> fileListA, List<File> fileListB) {
		final List<File> newList = new ArrayList<>();
		final Set<File> newSet = new HashSet<>();
		for (final File file : fileListA) if (newSet.add(file)) newList.add(file);
		for (final File file : fileListB) if (newSet.add(file)) newList.add(file);
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

	public static IRoot build(List<File> projectFileList, List<File> includePathList, boolean isReadable) {
		// todo: return project

		final List<File> projectFiles = createCanonicalAbsoluteFileList(projectFileList);
		final List<File> externalIncludePaths = createCanonicalAbsoluteFileList(includePathList);
		final List<File> internalIncludePaths = createInternalIncludePaths(projectFiles);
		final List<File> includePaths = combineCanonicalAbsoluteFileList(externalIncludePaths, internalIncludePaths);

		final char[] fileContentCharArray = PreprocessorBuilder.build(projectFiles, includePaths, isReadable);
		if (fileContentCharArray == null) return null;

		// todo: dbg
		{
			try (final FileWriter writer = new FileWriter("R:\\output.cpp")) {
				writer.write(fileContentCharArray);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		final IASTTranslationUnit translationUnit = TranslationUnitBuilder.build(fileContentCharArray);
		if (translationUnit == null) return null;

		// todo: dbg
		{
			try (final FileOutputStream fileOutputStream = new FileOutputStream("R:\\preprocessed.log")) {
				try (final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 65536)) {
					try (final PrintStream printStream = new PrintStream(bufferedOutputStream, false)) {
						_debugPrinter(printStream, 0, translationUnit, translationUnit);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		final IRoot root = AstBuilder.build(translationUnit);

		// todo: dbg
		{
			try (final FileWriter fileWriter = new FileWriter("R:\\tree_new.log")) {
				fileWriter.write(root.toTreeString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return root;
	}
}
