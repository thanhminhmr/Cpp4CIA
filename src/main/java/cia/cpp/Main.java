package cia.cpp;

import cia.cpp.builder.VersionBuilder;
import cia.cpp.database.Database;
import cia.cpp.differ.VersionDiffer;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public final class Main {
	private static final List<String> EMPTY_STRING_LIST = List.of();

	private final long startTime = System.nanoTime();

	private Main() {
	}

	public static void main(String[] argv) {
		if (argv.length != 2) {
			System.out.println("Usage: VersionBuilder.jar <input.ini> <output_path>");
			return;
		}
		new Main().build(Path.of(argv[0]), Path.of(argv[1]));
	}

	private void doLogging(String message) {
		System.out.printf("[%4.3f] %s\n", (System.nanoTime() - startTime) / 1000000000.0, message);
	}

	private void build(Path inputFilePath, Path outputPath) {
		try {
			final Map<String, ProjectVersion> versionMap = new HashMap<>();
			final List<VersionDifference> differenceList = new ArrayList<>();

			//noinspection MismatchedQueryAndUpdateOfCollection
			final Ini inputFile = new Ini(Files.newInputStream(inputFilePath, StandardOpenOption.READ));
			for (final Profile.Section section : inputFile.values()) {
				if (section.getName().startsWith("ProjectVersion")) {
					final ProjectVersion projectVersion = buildProjectVersion(section, outputPath);
					if (projectVersion != null) versionMap.put(projectVersion.getVersionName(), projectVersion);
				}
			}
			for (final Profile.Section section : inputFile.values()) {
				if (section.getName().startsWith("VersionDifference")) {
					final VersionDifference difference = buildVersionDifference(section, outputPath, versionMap);
					if (difference != null) differenceList.add(difference);
				}
			}
			for (final Profile.Section section : inputFile.values()) {
				if (section.getName().equals("Project")) {
					final Project project = buildProject(section, outputPath, List.copyOf(versionMap.values()), differenceList);
					if (project != null) {
						final String databaseFile = section.get("exportDatabase", "");
						if (!databaseFile.isBlank()) {
							doLogging("Exporting Project " + project.getProjectName() + " to database...");
							if (Database.exportProject(project, Path.of(databaseFile))) {
								doLogging("Success exporting Project " + project.getProjectName() + " to database");
							} else {
								doLogging("Failed exporting Project " + project.getProjectName() + " to database");
							}
						}
					}
				}
			}
		} catch (InvalidFileFormatException e) {
			doLogging("Invalid config file format! Config file: " + inputFilePath.toString());
		} catch (IOException e) {
			doLogging("Failed reading from config file! Config file: " + inputFilePath.toString());
		}
	}

	private ProjectVersion buildProjectVersion(Profile.Section section, Path outputPath) {
		final String inputFile = section.get("inputFile", "");
		if (inputFile.isBlank()) {
			final String versionName = section.get("versionName", "");
			if (versionName.isBlank()) {
				doLogging("Invalid config file format, missing versionName=\"" + versionName + "\"!");
				return null;
			}

			final Path projectRoot = Path.of(section.get("projectRoot", ""));
			final List<Path> projectFiles = Objects.requireNonNullElse(section.getAll("projectFile"), EMPTY_STRING_LIST)
					.stream().map(Path::of).collect(Collectors.toList());
			final List<Path> includePaths = Objects.requireNonNullElse(section.getAll("includePath"), EMPTY_STRING_LIST)
					.stream().map(Path::of).collect(Collectors.toList());

			doLogging("Building ProjectVersion " + versionName + "...");

			final ProjectVersion projectVersion = VersionBuilder.build(versionName, projectRoot, projectFiles, includePaths, false);
			if (projectVersion != null) {
				final String outputFileString = section.get("outputFile", "");
				final Path outputFilePath = outputFileString.isBlank() ? outputPath.resolve(versionName + ".ProjectVersion") : Path.of(outputFileString);
				try (final OutputStream outputStream = Files.newOutputStream(outputFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					projectVersion.toOutputStream(outputStream);
					doLogging("Success building ProjectVersion " + versionName);
					return projectVersion;
				} catch (IOException e) {
					doLogging("Failed writing to ProjectVersion file! ProjectVersion file: " + outputFilePath.toString());
				}
			} else {
				doLogging("Failed building ProjectVersion " + versionName);
			}
		} else {
			doLogging("Loading ProjectVersion from file " + inputFile + "...");
			try (final InputStream inputStream = Files.newInputStream(Path.of(inputFile), StandardOpenOption.READ)) {
				final ProjectVersion projectVersion = ProjectVersion.fromInputStream(inputStream);
				doLogging("Success loading ProjectVersion from file " + inputFile);
				return projectVersion;
			} catch (IOException e) {
				doLogging("Failed reading from file! ProjectVersion file: " + inputFile);
			}
		}
		return null;
	}

	private VersionDifference buildVersionDifference(Profile.Section section, Path outputPath, Map<String, ProjectVersion> versionMap) {
		final String inputFile = section.get("inputFile", "");
		if (inputFile.isBlank()) {
			final ProjectVersion versionA = versionMap.get(section.get("versionA", ""));
			final ProjectVersion versionB = versionMap.get(section.get("versionB", ""));
			if (versionA == null || versionB == null) {
				doLogging("Invalid config file format, missing versionA=\"" + versionA + "\" or/and versionB=\"" + versionB + "\"!");
				return null;
			}

			doLogging("Building VersionDifference " + versionA.getVersionName() + "-" + versionB.getVersionName() + "...");

			final VersionDifference versionDifference = VersionDiffer.compare(versionA, versionB);
			final String outputFileString = section.get("outputFile", "");
			final Path outputFilePath = outputFileString.isBlank() ? outputPath.resolve(versionA.getVersionName() + "-" + versionB.getVersionName() + ".VersionDifference") : Path.of(outputFileString);
			try (final OutputStream outputStream = Files.newOutputStream(outputFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				versionDifference.toOutputStream(outputStream);
				doLogging("Success building VersionDifference " + versionA.getVersionName() + "-" + versionB.getVersionName());
				return versionDifference;
			} catch (IOException e) {
				doLogging("Failed writing to output file! Output file: " + outputFilePath.toString());
			}
		} else {
			doLogging("Loading VersionDifference from file " + inputFile + "...");
			try (final InputStream inputStream = Files.newInputStream(Path.of(inputFile), StandardOpenOption.READ)) {
				final VersionDifference difference = VersionDifference.fromInputStream(inputStream);
				doLogging("Success loading VersionDifference from file " + inputFile);
				return difference;
			} catch (IOException e) {
				doLogging("Failed reading from input file! Input file: " + inputFile);
			}
		}
		return null;
	}

	private Project buildProject(Profile.Section section, Path outputPath, List<ProjectVersion> versionList, List<VersionDifference> differenceList) {
		final String inputFile = section.get("inputFile", "");
		if (inputFile.isBlank()) {
			final String projectName = section.get("projectName", "");
			if (projectName.isBlank()) {
				doLogging("Invalid config file format, missing projectName=\"" + projectName + "\"!");
				return null;
			}

			doLogging("Building Project " + projectName + "...");

			final Project project = Project.of(projectName, versionList, differenceList);
			final String outputFileString = section.get("outputFile", "");
			final Path outputFilePath = outputFileString.isBlank() ? outputPath.resolve(projectName + ".Project") : Path.of(outputFileString);
			try (final OutputStream outputStream = Files.newOutputStream(outputFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
				project.toOutputStream(outputStream);
				doLogging("Success building Project " + projectName);
				return project;
			} catch (IOException e) {
				doLogging("Failed writing to Project file! Project file: " + outputFilePath.toString());
			}
		} else {
			doLogging("Loading Project from file " + inputFile + "...");
			try (final InputStream inputStream = Files.newInputStream(Path.of(inputFile), StandardOpenOption.READ)) {
				final Project project = Project.fromInputStream(inputStream);
				doLogging("Success loading Project from file " + inputFile);
				return project;
			} catch (IOException e) {
				doLogging("Failed reading from input file! Input file: " + inputFile);
			}
		}
		return null;
	}
}
