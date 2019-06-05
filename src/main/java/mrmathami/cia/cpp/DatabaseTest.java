package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.VersionBuilder;
import mrmathami.cia.cpp.builder.VersionBuilderDebugger;
import mrmathami.cia.cpp.database.Database;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class DatabaseTest {
	private DatabaseTest() {
	}

	public static void main(String[] argv) throws IOException {
		long start_time = System.nanoTime();

		final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\");
		final List<Path> projectFiles =
//				List.of(Path.of("D:\\Research\\SourceCodeComparator\\test\\tiny_but_decent\\Test\\Source.cpp"));
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//              readConfigFile(Path.of("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
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

		final List<Path> includePaths = List.of();

		final VersionBuilderDebugger debugger = new VersionBuilderDebugger();
		debugger.setSaveFileContent(true);
		debugger.setSaveTranslationUnit(true);
		debugger.setSaveRoot(true);

		final ProjectVersion projectVersion = VersionBuilder.build("CmderLauncher", projectRoot, projectFiles, includePaths, debugger);
		if (projectVersion == null) return;

		debugger.debugOutput(projectRoot);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		try (final FileOutputStream fos = new FileOutputStream("R:\\CmderLauncher.proj")) {
			projectVersion.toOutputStream(fos);
		}

		debugger.debugOutput(projectRoot);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final Project project = Project.of("tesseract", List.of(projectVersion), List.of());

		Database.exportProject(project, Path.of("R:\\"));
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
