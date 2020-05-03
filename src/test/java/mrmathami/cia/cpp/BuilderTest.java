package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.ProjectVersion;
import mrmathami.cia.cpp.builder.VersionBuilder;
import mrmathami.cia.cpp.builder.VersionBuilderDebugger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

		final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\cppcia\\test");
		final List<Path> projectFiles =
				List.of(
						projectRoot.resolve("main.cpp")
				);


		final List<Path> includePaths = List.of();

		final VersionBuilderDebugger debugger = new VersionBuilderDebugger();
		debugger.setLoadFileContent(false);
		debugger.setSaveTranslationUnit(true);
		debugger.setOutputPath(projectRoot);

		final ProjectVersion projectVersion = VersionBuilder.build("test", projectRoot, projectFiles, includePaths, VersionBuilder.WEIGHT_MAP, debugger);


		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		try (final OutputStream outputStream = Files.newOutputStream(projectRoot.resolve(
				projectVersion.getVersionName() + ".proj"),
				StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
			projectVersion.toOutputStream(outputStream);
		}

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
