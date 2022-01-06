package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.ProjectVersion;
import mrmathami.cia.cpp.builder.VersionBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class BuilderTest {
	private static final Set<String> EXTENSIONS = Set.of(
			".c", ".cc", ".cpp", ".c++", ".cxx",
			".h", ".hh", ".hpp", ".h++", ".hxx"
	);

	private BuilderTest() {
	}

	public static Path appendPath(Path path, String childPath) {
		return StreamSupport.stream(Path.of(childPath).spliterator(), false).reduce(path, Path::resolve);
	}

	private static boolean fileFilter(Path path) {
		final String file = path.getFileName().toString();
		final int dot = file.lastIndexOf('.');
		return dot >= 0 && EXTENSIONS.contains(file.substring(dot).toLowerCase(Locale.ROOT));
	}

	public static ProjectVersion createProjectVersion(String partId, Path extractPath, InputStream stream,
			String sourceFolder, String includeFolder) throws IOException, CppException {
		final List<Path> projectFiles = new ArrayList<>();
		final List<Path> includePaths = new ArrayList<>();
		try (final ZipInputStream zipStream = new ZipInputStream(stream)) {
			final Path sourcePath = sourceFolder.isBlank() ? extractPath : appendPath(extractPath, sourceFolder);
			final Path includePath = includeFolder.isBlank() ? null : appendPath(extractPath, includeFolder);
			while (true) {
				final ZipEntry entry = zipStream.getNextEntry();
				if (entry == null) break;

				final Path outputPath = appendPath(extractPath, entry.getName());
				if (entry.isDirectory()) {
					if (outputPath.startsWith(sourcePath)
							|| includePath != null && outputPath.startsWith(includePath)) {
						Files.createDirectories(outputPath);
					}
				} else if (fileFilter(outputPath)) {
					if (outputPath.startsWith(sourcePath)) {
						projectFiles.add(outputPath);
					} else if (includePath != null && outputPath.startsWith(includePath)) {
						includePaths.add(outputPath);
					} else {
						continue;
					}
					Files.copy(zipStream, outputPath);
				}
			}
		}
		return VersionBuilder.build(partId, extractPath, projectFiles, includePaths, VersionBuilder.WEIGHT_MAP);
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
//		System.in.read();
		final long start_time = System.nanoTime();

		final String id = UUID.randomUUID().toString();
		final Path extractPath = Path.of("/tmp").resolve(id);

//		final Path inputZip = Path.of("/home/meo/Downloads/zpaq715.zip");
//		final Path outputProject = Path.of("zpaq715.proj");
		final Path inputZip = Path.of("/home/meo/Documents/PrusaSlicer_new.zip");
		final Path outputProject = Path.of("/home/meo/Documents/PrusaSlicer_new.proj");

		try (final InputStream inputStream = Files.newInputStream(inputZip)) {
			Files.createDirectories(extractPath);
			final ProjectVersion projectVersion = createProjectVersion(id, extractPath, inputStream,
					"/src", "");

			System.out.println((System.nanoTime() - start_time) / 1000000.0);

			try (final OutputStream outputStream = Files.newOutputStream(outputProject)) {
				projectVersion.toOutputStream(outputStream);
				System.out.println((System.nanoTime() - start_time) / 1000000.0);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
}
