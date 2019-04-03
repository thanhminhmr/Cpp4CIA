package mrmathami.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public final class Utilities {
	private static final char[] EMPTY_CHARS = new char[0];

	private Utilities() {
	}

	public static String getCanonicalAbsolutePath(File file) {
		try {
			return file.getCanonicalPath();
		} catch (IOException ignored) {
			return file.getAbsolutePath();
		}
	}

	public static String getCanonicalAbsolutePath(String path) {
		return getCanonicalAbsolutePath(new File(path));
	}

	public static File getCanonicalAbsoluteFile(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException ignored) {
			return file.getAbsoluteFile();
		}
	}

	public static File getCanonicalAbsoluteFile(String path) {
		return getCanonicalAbsoluteFile(new File(path));
	}

	public static char[] readFile(File file) {
		final StringBuilder content = new StringBuilder();
		final char[] fileBuffer = new char[65536]; // 64k at a time, fast
		try (final FileReader fileReader = new FileReader(file)) {
			int length = fileReader.read(fileBuffer);
			while (length != -1) {
				content.append(fileBuffer, 0, length);
				length = fileReader.read(fileBuffer);
			}
		} catch (IOException e) {
			return EMPTY_CHARS;
		}
		final char[] fileContent = new char[content.length()];
		content.getChars(0, content.length(), fileContent, 0);
		return fileContent;
	}
}
