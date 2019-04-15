package cia.cpp;

import cia.cpp.builder.VersionBuilder;
import cia.cpp.database.Database;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public final class DatabaseTest {
	private DatabaseTest() {
	}

	public static void main(String[] argv) throws IOException {
		long start_time = System.nanoTime();

		final Path projectRoot = Paths.get("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src");
		final List<Path> projectFiles =
//				List.of(
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//				BuilderTest.readConfigFile(Paths.get("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
				List.of(
						Paths.get("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\main.cpp"),
						Paths.get("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
						Paths.get("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.h")
				);
//				List.of(
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Array.h"),
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Bitmap.h"),
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Buffer.h"),
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\NeuralNetwork.h"),
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Pixel.h"),
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Randomizer.h"),
//						Paths.get("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Trainer.cpp")
//				);

		final List<Path> includePaths = List.of();
		final ProjectVersion projectVersion = VersionBuilder.build("tesseract", projectRoot, projectFiles, includePaths, false);
		if (projectVersion == null) return;

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final Project project = Project.of("tesseract", List.of(projectVersion), List.of());

		Database.exportProject(project, new File("R:\\"));
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
