package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.ast.RootNode;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Arrays;

public final class VersionBuilderDebugger {
	private boolean readable;
	private boolean saveFileContent;
	private boolean saveTranslationUnit;
	private boolean saveRoot;

	private String versionName;
	private char[] fileContent;
	private IASTTranslationUnit translationUnit;
	private RootNode root;

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

	public final boolean isReadable() {
		return readable;
	}

	public final void setReadable(boolean readable) {
		this.readable = readable;
	}

	public final boolean isSaveFileContent() {
		return saveFileContent;
	}

	public final void setSaveFileContent(boolean saveFileContent) {
		this.saveFileContent = saveFileContent;
	}

	public final boolean isSaveTranslationUnit() {
		return saveTranslationUnit;
	}

	public final void setSaveTranslationUnit(boolean saveTranslationUnit) {
		this.saveTranslationUnit = saveTranslationUnit;
	}

	public final boolean isSaveRoot() {
		return saveRoot;
	}

	public final void setSaveRoot(boolean saveRoot) {
		this.saveRoot = saveRoot;
	}

	public String getVersionName() {
		return versionName;
	}

	void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public final char[] getFileContent() {
		return fileContent;
	}

	final void setFileContent(char[] fileContent) {
		this.fileContent = fileContent;
	}

	public final IASTTranslationUnit getTranslationUnit() {
		return translationUnit;
	}

	final void setTranslationUnit(IASTTranslationUnit translationUnit) {
		this.translationUnit = translationUnit;
	}

	public final RootNode getRoot() {
		return root;
	}

	final void setRoot(RootNode root) {
		this.root = root;
	}

	public final void debugOutput(Path outputPath) {
		try {
			if (saveFileContent) {
				try (final FileWriter writer = new FileWriter(outputPath.resolve("output_" + versionName + ".cpp").toString())) {
					writer.write(fileContent);
				}
			}

			if (saveTranslationUnit) {
				try (final FileOutputStream fileOutputStream = new FileOutputStream(outputPath.resolve("preprocessed_" + versionName + ".log").toString())) {
					try (final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 65536)) {
						try (final PrintStream printStream = new PrintStream(bufferedOutputStream, false)) {
							_debugPrinter(printStream, 0, translationUnit, translationUnit);
						}
					}
				}
			}

			if (saveRoot) {
				try (final FileWriter fileWriter = new FileWriter(outputPath.resolve("tree_" + versionName + ".log").toString())) {
					fileWriter.write(root.toTreeString());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
