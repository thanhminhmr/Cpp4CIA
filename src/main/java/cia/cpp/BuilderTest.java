package cia.cpp;

import cia.cpp.builder.ProjectVersionBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BuilderTest {
	private BuilderTest() {
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
		//System.in.read();
		long start_time = System.nanoTime();
		final List<File> projectFiles =
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
				readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\main.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.h")
//				);
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Array.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Bitmap.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Buffer.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\NeuralNetwork.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Pixel.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Randomizer.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Trainer.cpp")
//				);

		final List<File> includePaths = List.of();
		final ProjectVersion projectVersion = ProjectVersionBuilder.build("tesseract", projectFiles, includePaths, false);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		if (projectVersion == null) return;

		try (final FileOutputStream fos = new FileOutputStream("R:\\output.proj")) {
			projectVersion.toOutputStream(fos);
		}

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
