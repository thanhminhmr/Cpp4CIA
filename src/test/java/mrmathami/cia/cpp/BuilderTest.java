package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.VersionBuilder;
import mrmathami.cia.cpp.builder.VersionBuilderDebugger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BuilderTest {
	private BuilderTest() {
	}

	public static List<Path> readConfigFile(Path configPath) throws IOException {
		final List<String> filePaths = Files.readAllLines(configPath, StandardCharsets.UTF_8);
		final List<Path> fileList = new ArrayList<>();
		final Set<Path> fileSet = new HashSet<>();
		for (final String pathString : filePaths) {
			if (!pathString.isBlank()) {
				final Path filePath = Path.of(pathString).toRealPath();
				if (fileSet.add(filePath)) fileList.add(filePath);
			}
		}
		return fileList;
	}

	public static void main(String[] args) throws Exception {
		//System.in.read();
		long start_time = System.nanoTime();

		final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.0");
		final List<Path> projectFiles =
				List.of(
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.0\\main.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\TinyEXIF-1.0.0\\TinyEXIF.h")
				);


		final List<Path> includePaths = List.of();

		final VersionBuilderDebugger debugger = new VersionBuilderDebugger();
		debugger.setSaveTranslationUnit(true);
		debugger.setOutputPath(Path.of("C:\\WINDOWS\\TEMP\\Temp"));

		final ProjectVersion projectVersion = VersionBuilder.build("CmderLauncher", projectRoot, projectFiles, includePaths, debugger);


		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\CmderLauncher.proj")) {
			projectVersion.toOutputStream(fos);
		}

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
