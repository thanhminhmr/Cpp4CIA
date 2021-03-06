package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.ProjectVersion;
import mrmathami.cia.cpp.builder.VersionBuilder;
import mrmathami.cia.cpp.builder.VersionBuilderDebugger;
import mrmathami.cia.cpp.differ.VersionDiffer;
import mrmathami.cia.cpp.differ.VersionDifferDebugger;
import mrmathami.cia.cpp.differ.VersionDifference;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class SerializerTest {
	private SerializerTest() {
	}

	public static void main(String[] args) throws IOException, CppException {
//		System.in.read();
		long start_time = System.nanoTime();
		{
			final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\zpaq714");
//			final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\TinyEXIF-1.0.0");
			final List<Path> projectFiles =
				List.of(
						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\zpaq714\\zpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\zpaq714\\libzpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\zpaq714\\libzpaq.h")
				);
//				BuilderTest.readConfigFile(new File("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\tesseract-4.0.0\\src\\a.txt"));
//					List.of(
//							Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\TinyEXIF-1.0.0\\main.cpp"),
//							Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
//							Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\TinyEXIF-1.0.0\\TinyEXIF.h")
//					);
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Array.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Bitmap.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Buffer.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\NeuralNetwork.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Pixel.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Randomizer.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Trainer.cpp")
//				);

			final List<Path> includePaths = List.of();

			final VersionBuilderDebugger debugger = new VersionBuilderDebugger();
			debugger.setSaveTranslationUnit(false);
			debugger.setOutputPath(Path.of("C:\\WINDOWS\\TEMP\\Temp\\"));

//			final ProjectVersion projectVersion = VersionBuilder.build("project1", projectRoot, projectFiles, includePaths);
			final ProjectVersion projectVersion = VersionBuilder.build("project1", projectRoot, projectFiles, includePaths, VersionBuilder.WEIGHT_MAP, debugger);


			try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\project1.ProjectVersion")) {
				projectVersion.toOutputStream(fos);
			}

			System.out.println((System.nanoTime() - start_time) / 1000000.0);


			final Path projectRoot2 = Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\zpaq715");
//			final Path projectRoot2 = Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\TinyEXIF-1.0.1");
			final List<Path> projectFiles2 =
				List.of(
						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\zpaq715\\zpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\zpaq715\\libzpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\zpaq715\\libzpaq.h")
				);
//				BuilderTest.readConfigFile(new File("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\tesseract-4.0.0\\src\\a.txt"));
//					List.of(
//							Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\TinyEXIF-1.0.1\\main.cpp"),
//							Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\TinyEXIF-1.0.1\\TinyEXIF.cpp"),
//							Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\TinyEXIF-1.0.1\\TinyEXIF.h")
//					);
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Array.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Bitmap.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Buffer.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\NeuralNetwork.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Pixel.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Randomizer.h"),
//						Path.of("D:\\Research\\SourceCodeComparator\\cia\\test\\cpp\\meo_nn\\Trainer.cpp")
//				);

			final List<Path> includePaths2 = List.of();
			final ProjectVersion projectVersion2 = VersionBuilder.build("project2", projectRoot2, projectFiles2, includePaths2, VersionBuilder.WEIGHT_MAP, debugger);
//			final ProjectVersion projectVersion2 = VersionBuilder.build("project2", projectRoot2, projectFiles2, includePaths2);

			try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\project2.ProjectVersion")) {
				projectVersion2.toOutputStream(fos);
			}

			System.out.println((System.nanoTime() - start_time) / 1000000.0);
		}
		ProjectVersion projectVersion, projectVersion2;

		try (final FileInputStream fileInputStream = new FileInputStream("C:\\WINDOWS\\TEMP\\Temp\\project1.ProjectVersion")) {
			projectVersion = ProjectVersion.fromInputStream(fileInputStream);
		}
		try (final FileInputStream fileInputStream = new FileInputStream("C:\\WINDOWS\\TEMP\\Temp\\project2.ProjectVersion")) {
			projectVersion2 = ProjectVersion.fromInputStream(fileInputStream);
		}

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final VersionDifference difference = VersionDiffer.compare(projectVersion, projectVersion2, VersionDiffer.IMPACT_WEIGHT_MAP);

		VersionDifferDebugger.debugOutput(Path.of("C:\\WINDOWS\\TEMP\\Temp\\"), difference);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
		try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\project1_project2.VersionDifference")) {
			difference.toOutputStream(fos);
		}
		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final Project project = new Project("project", List.of(projectVersion, projectVersion2), List.of(difference));
		try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\project.Project")) {
			project.toOutputStream(fos);
		}
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
