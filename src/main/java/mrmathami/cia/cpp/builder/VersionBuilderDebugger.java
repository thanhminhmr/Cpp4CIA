package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.ast.RootNode;
import org.eclipse.cdt.core.dom.ast.IASTImageLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public final class VersionBuilderDebugger {
	private boolean readableFileContent;
	private boolean loadFileContent;
	private boolean saveTranslationUnit;

	private String versionName;
	private Path outputPath;

	private char[] fileContent;

	private static void _printTranslationUnit(Writer writer, IASTTranslationUnit translationUnit, IASTNode node, int level) throws IOException {
		final String raw = node.getRawSignature();
		final int cr = raw.indexOf('\r');
		final int lf = raw.indexOf('\n');
		final int line = (cr < 0 || lf < 0) ? Integer.max(cr, lf) : Integer.min(cr, lf);
		final String rawSub = (line < 0 || line > 60) ? (raw.length() > 60 ? raw.substring(0, 60) : raw) : raw.substring(0, line);

		if (node instanceof IASTName) {
			IBinding iBinding = ((IASTName) node).resolveBinding();
			IASTName[] names = iBinding != null ? translationUnit.getDeclarationsInAST(iBinding) : null;

			writer.write(String.format(
					"%" + (level != 0 ? (level * 2) : "") + "s%-" + (100 - level * 2 - 13) + "s (0x%08X) + %-60s + %-30s | %-50s | %-50s | %s\n",
					"",
					node.getClass().getSimpleName(), node.hashCode(),
					rawSub,
					node.getFileLocation(),
					iBinding != null ? String.format("(0x%08X) %s", iBinding.hashCode(), iBinding.getClass().getSimpleName()) : null,
					iBinding != null ? iBinding.getName() : null,
					iBinding != null ? Arrays.toString(Arrays.stream(names).map(name -> {
						if (name == null) return "{ null } ";
						IASTImageLocation location = name.getImageLocation();
						if (location == null) return "{ " + name.toString() + " } ";
						return "{ " + name.toString() + ", " + location.getFileName() + "["
								+ location.getNodeOffset() + ", " + (location.getNodeOffset()
								+ location.getNodeLength()) + "] } ";
					}).toArray()) : null
			));
		} else {
			writer.write(String.format(
					"%" + (level != 0 ? (level * 2) : "") + "s%-" + (100 - level * 2 - 13) + "s (0x%08X) | %-60s | %s\n",
					"",
					node.getClass().getSimpleName(), node.hashCode(),
					rawSub,
					node.getFileLocation()
			));
		}

		for (IASTNode child : node.getChildren()) {
			_printTranslationUnit(writer, translationUnit, child, level + 1);
		}
	}

	public final boolean isReadableFileContent() {
		return readableFileContent;
	}

	public final void setReadableFileContent(boolean readableFileContent) {
		this.readableFileContent = readableFileContent;
	}

	public final boolean isLoadFileContent() {
		return loadFileContent;
	}

	public final void setLoadFileContent(boolean loadFileContent) {
		this.loadFileContent = loadFileContent;
	}

	public final boolean isSaveTranslationUnit() {
		return saveTranslationUnit;
	}

	public final void setSaveTranslationUnit(boolean saveTranslationUnit) {
		this.saveTranslationUnit = saveTranslationUnit;
	}

	public final String getVersionName() {
		return versionName;
	}

	public final void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public final Path getOutputPath() {
		return outputPath;
	}

	public final void setOutputPath(Path outputPath) {
		this.outputPath = outputPath;
	}


	void saveFileContent(char[] fileContent) {
		try (final Writer writer = Files.newBufferedWriter(outputPath.resolve("output_" + versionName + ".cpp"),
				StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			writer.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void saveTranslationUnit(IASTTranslationUnit translationUnit) {
		try (final Writer writer = Files.newBufferedWriter(outputPath.resolve("preprocessed_" + versionName + ".log"),
				StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			_printTranslationUnit(writer, translationUnit, translationUnit, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void saveRoot(RootNode root) {
		try (final Writer writer = Files.newBufferedWriter(outputPath.resolve("tree_" + versionName + ".log"),
				StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			writer.write(root.toTreeString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	boolean loadFileContent() {
		if (loadFileContent) {
			try {
				this.fileContent = Files.readString(outputPath.resolve("output_" + versionName + ".cpp"), StandardCharsets.UTF_8).toCharArray();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public char[] getFileContent() {
		return fileContent;
	}
}
