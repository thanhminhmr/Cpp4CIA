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

		final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq715");
//		final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\test\\sr2\\a");
		final List<Path> projectFiles =
//				List.of(Path.of("D:\\Research\\SourceCodeComparator\\test\\sr2\\a\\sr2.cpp"));
				List.of(
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq715\\zpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq715\\libzpaq.cpp"),
						Path.of("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq715\\libzpaq.h")
				);
//              readConfigFile(Path.of("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\main.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.h")
//				);
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
		debugger.setSaveTranslationUnit(false);
		debugger.setSaveRoot(true);

		final ProjectVersion projectVersion = VersionBuilder.build("zpaq715", projectRoot, projectFiles, includePaths, debugger);

		debugger.debugOutput(projectRoot);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		try (final FileOutputStream fos = new FileOutputStream("D:\\Research\\SourceCodeComparator\\java-cia\\testData\\cpp\\zpaq\\zpaq715.proj")) {
			projectVersion.toOutputStream(fos);
		}

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
