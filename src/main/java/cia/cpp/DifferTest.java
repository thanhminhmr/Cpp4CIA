package cia.cpp;

import cia.cpp.builder.VersionBuilder;
import cia.cpp.builder.VersionBuilderDebugger;
import cia.cpp.database.Database;
import cia.cpp.differ.VersionDiffer;
import cia.cpp.differ.VersionDifferDebugger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class DifferTest {
	private DifferTest() {
	}

	public static void main(String[] args) throws IOException {
		//System.in.read();
		long start_time = System.nanoTime();
		{
			final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0");
			final List<Path> projectFiles =
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//				BuilderTest.readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
					List.of(
							Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\main.cpp"),
							Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
							Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.h")
					);
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Array.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Bitmap.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Buffer.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\NeuralNetwork.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Pixel.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Randomizer.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Trainer.cpp")
//				);

			final List<Path> includePaths = List.of();

			final VersionBuilderDebugger debugger = new VersionBuilderDebugger();
			debugger.setSaveFileContent(true);
			debugger.setSaveTranslationUnit(true);
			debugger.setSaveRoot(true);

			final ProjectVersion projectVersion = VersionBuilder.build("project1", projectRoot, projectFiles, includePaths, debugger);
			if (projectVersion == null) return;

			debugger.debugOutput(projectRoot);

			try (final FileOutputStream fos = new FileOutputStream("R:\\project1.proj")) {
				projectVersion.toOutputStream(fos);
			}

			System.out.println((System.nanoTime() - start_time) / 1000000.0);


			final Path projectRoot2 = Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1");
			final List<Path> projectFiles2 =
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//				BuilderTest.readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
					List.of(
							Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\main.cpp"),
							Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.cpp"),
							Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.h")
					);
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Array.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Bitmap.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Buffer.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\NeuralNetwork.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Pixel.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Randomizer.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Trainer.cpp")
//				);

			final List<Path> includePaths2 = List.of();
			final ProjectVersion projectVersion2 = VersionBuilder.build("project2", projectRoot2, projectFiles2, includePaths2, debugger);
			if (projectVersion2 == null) return;

			debugger.debugOutput(projectRoot);

			try (final FileOutputStream fos = new FileOutputStream("R:\\project2.proj")) {
				projectVersion2.toOutputStream(fos);
			}

			System.out.println((System.nanoTime() - start_time) / 1000000.0);
		}

		ProjectVersion projectVersion, projectVersion2;

		try (final FileInputStream fileInputStream = new FileInputStream("R:\\project1.proj")) {
			projectVersion = ProjectVersion.fromInputStream(fileInputStream);
		}
		try (final FileInputStream fileInputStream = new FileInputStream("R:\\project2.proj")) {
			projectVersion2 = ProjectVersion.fromInputStream(fileInputStream);
		}

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final VersionDifferDebugger debugger = new VersionDifferDebugger();

		final VersionDifference difference = VersionDiffer.compare(projectVersion, projectVersion2, debugger);

		debugger.debugOutput(Path.of("R:\\"));

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
		try (final FileOutputStream fos = new FileOutputStream("R:\\project_project2.pcmp")) {
			difference.toOutputStream(fos);
		}
		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final Project project = Project.of("project", List.of(projectVersion, projectVersion2), List.of(difference));

		Database.exportProject(project, Path.of("R:\\"));
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
