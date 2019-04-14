package cia.cpp;

import cia.cpp.builder.VersionBuilder;
import cia.cpp.database.Database;

import java.io.File;
import java.io.IOException;
import java.util.List;

public final class DatabaseTest {
	private DatabaseTest() {
	}

	public static void main(String[] argv) throws IOException {
		long start_time = System.nanoTime();
		final List<File> projectFiles =
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//				BuilderTest.readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
				List.of(
						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\main.cpp"),
						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.h")
				);
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
		final ProjectVersion projectVersion = VersionBuilder.build("tesseract", projectFiles, includePaths, false);
		if (projectVersion == null) return;

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final Project project = Project.of("tesseract", List.of(projectVersion), List.of());

		Database.exportProject(project, new File("R:\\"));
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
