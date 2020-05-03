package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.VersionBuilder;
import mrmathami.cia.cpp.builder.VersionBuilderDebugger;
import mrmathami.cia.cpp.differ.VersionDiffer;
import mrmathami.cia.cpp.differ.VersionDifferDebugger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class DifferTest {
	private DifferTest() {
	}

	public static void main(String[] args) throws IOException, CppException {
//		System.in.read();
		long start_time = System.nanoTime();

		final VersionBuilderDebugger debugger = new VersionBuilderDebugger();
		debugger.setLoadFileContent(false);
		debugger.setSaveTranslationUnit(false);
		debugger.setOutputPath(Path.of("C:\\WINDOWS\\TEMP\\Temp\\"));

		// ==========
		final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq714");
		final List<Path> projectFiles =
				List.of(
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq714\\zpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq714\\libzpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq714\\libzpaq.h")
				);

//		final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.0");
//		final List<Path> projectFiles =
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.0\\main.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.0\\TinyEXIF.h")
//				);

		final List<Path> includePaths = List.of();

		final ProjectVersion projectVersion = VersionBuilder.build("project1", projectRoot, projectFiles, includePaths, debugger);


		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		// ==========
		final Path projectRoot2 = Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq715");
		final List<Path> projectFiles2 =
				List.of(
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq715\\zpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq715\\libzpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq715\\libzpaq.h")
				);

//		final Path projectRoot2 = Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.1");
//		final List<Path> projectFiles2 =
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.1\\main.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.1\\TinyEXIF.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.1\\TinyEXIF.h")
//				);

		final List<Path> includePaths2 = List.of();
		final ProjectVersion projectVersion2 = VersionBuilder.build("project2", projectRoot2, projectFiles2, includePaths2, debugger);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		// ==========

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final VersionDifference difference = VersionDiffer.compare(projectVersion, projectVersion2);

		VersionDifferDebugger.debugOutput(Path.of("C:\\WINDOWS\\TEMP\\Temp\\"), projectVersion, projectVersion2,
				difference.getAddedNodes(), difference.getRemovedNodes(), difference.getChangedNodes(),
				difference.getUnchangedNodes(), difference.getImpactWeightMap());

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
		try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\project1_project2.VersionDifference")) {
			difference.toOutputStream(fos);
		}
		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final Project project = Project.of("project", List.of(projectVersion, projectVersion2), List.of(difference));
		try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\project.Project")) {
			project.toOutputStream(fos);
		}
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
