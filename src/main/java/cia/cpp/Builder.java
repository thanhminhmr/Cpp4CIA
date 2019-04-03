package cia.cpp;

import cia.cpp.builder.ProjectBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Builder {
	private Builder() {
	}

	private static List<File> readConfigFile(File file) throws IOException {
		final StringBuilder content = new StringBuilder();
		final char[] fileBuffer = new char[65536]; // 64k at a time, fast
		try (final FileReader fileReader = new FileReader(file)) {
			int length = fileReader.read(fileBuffer);
			while (length != -1) {
				content.append(fileBuffer, 0, length);
				length = fileReader.read(fileBuffer);
			}
		}
		final List<File> fileList = new ArrayList<>();
		final Set<File> fileSet = new HashSet<>();
		for (final String filePath : content.toString().split("[\r\n]+")) {
			if (filePath != null && !filePath.isBlank()) {
				final File fileInSet = new File(filePath);
				if (fileInSet.exists() && fileSet.add(fileInSet)) {
					fileList.add(fileInSet);
				}
			}
		}
		return fileList;
	}

	public static void main(String[] args) throws Exception {
		System.in.read();
		long start_time = System.nanoTime();
		ProjectBuilder.build(
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				)
				readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"))
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\main.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.h")
//				)
				, List.of(), false
		);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
