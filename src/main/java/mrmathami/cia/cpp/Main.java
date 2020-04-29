package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.VersionBuilder;
import mrmathami.cia.cpp.builder.VersionBuilderDebugger;
import mrmathami.cia.cpp.differ.VersionDiffer;
import mrmathami.util.Utilities;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Main {
	private static final List<String> EMPTY_STRING_LIST = List.of();

	private final long startTime = System.nanoTime();

	private final VersionBuilderDebugger builderDebugger;

	private Main(int debugLevel) {
		this.builderDebugger = debugLevel > 0 ? new VersionBuilderDebugger() : null;
		if (debugLevel > 1) builderDebugger.setSaveRoot(true);
		if (debugLevel > 2) builderDebugger.setSaveFileContent(true);
		if (debugLevel > 3) builderDebugger.setSaveTranslationUnit(true);
	}

	public static void main(String[] argv) {
		if (argv.length < 2 || argv.length > 3) {
			System.out.println("Usage: cpp4cia.jar <input.ini> [debugLevel]");
			return;
		}
		new Main(argv.length == 3 ? Integer.parseInt(argv[1]) : 0).build(Path.of(argv[0]));
	}

	private void doLogging(String message) {
		final String header = String.format("[%4.3fs] ", (System.nanoTime() - startTime) / 1000000000.0);
		System.out.println(header.concat(message).strip().replace("\n", "\n" + header));
	}

	private void build(Path inputFilePath) {
		try {
			final Map<String, ProjectVersion> versionMap = new HashMap<>();
			final List<VersionDifference> differenceList = new ArrayList<>();

			//noinspection MismatchedQueryAndUpdateOfCollection
			final Ini inputFile = new Ini(Files.newInputStream(inputFilePath, StandardOpenOption.READ));
			for (final Profile.Section section : inputFile.values()) {
				if (section.getName().startsWith("ProjectVersion")) {
					final ProjectVersion projectVersion = buildProjectVersion(section);
					if (projectVersion != null) versionMap.put(projectVersion.getVersionName(), projectVersion);
				}
			}
			for (final Profile.Section section : inputFile.values()) {
				if (section.getName().startsWith("VersionDifference")) {
					final VersionDifference difference = buildVersionDifference(section, versionMap);
					if (difference != null) differenceList.add(difference);
				}
			}
			for (final Profile.Section section : inputFile.values()) {
				if (section.getName().equals("Project")) {
					buildProject(section, List.copyOf(versionMap.values()), differenceList);
				}
			}
		} catch (InvalidFileFormatException e) {
			doLogging("Invalid config file format! Config file: " + inputFilePath.toString()
					+ "\nException: " + Utilities.exceptionToString(e));
		} catch (IOException e) {
			doLogging("Failed reading from config file! Config file: " + inputFilePath.toString()
					+ "\nException: " + Utilities.exceptionToString(e));
		}
	}

	private ProjectVersion buildProjectVersion(Profile.Section section) {
		final String inputFile = section.get("inputFile", "");
		if (inputFile.isBlank()) {
			final String versionName = section.get("versionName", "");
			if (versionName.isBlank()) {
				doLogging("Invalid config file ProjectVersion format, missing versionName=\"" + versionName + "\"!");
				return null;
			}

			final Path projectRoot = Path.of(section.get("projectRoot", ""));
			final List<Path> projectFiles = Objects.requireNonNullElse(section.getAll("projectFile"), EMPTY_STRING_LIST)
					.stream().map(Path::of).collect(Collectors.toList());
			final List<Path> includePaths = Objects.requireNonNullElse(section.getAll("includePath"), EMPTY_STRING_LIST)
					.stream().map(Path::of).collect(Collectors.toList());

			doLogging("Building ProjectVersion " + versionName + "...");

			try {
				final ProjectVersion projectVersion = builderDebugger != null
						? VersionBuilder.build(versionName, projectRoot, projectFiles, includePaths, builderDebugger)
						: VersionBuilder.build(versionName, projectRoot, projectFiles, includePaths);
				if (builderDebugger != null) builderDebugger.debugOutput(projectRoot);

				final String outputFileString = section.get("outputFile", "");
				if (!outputFileString.isBlank()) {
					final Path outputFilePath = Path.of(outputFileString);
					try (final OutputStream outputStream = Files.newOutputStream(outputFilePath,
							StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
						projectVersion.toOutputStream(outputStream);
						doLogging("Success building ProjectVersion " + versionName);
						return projectVersion;
					} catch (IOException e) {
						doLogging("Failed writing ProjectVersion to output file! ProjectVersion file: " + outputFilePath.toString()
								+ "\nException: " + Utilities.exceptionToString(e));
					}
				} else {
					doLogging("Skip writing ProjectVersion to output file.");
					return projectVersion;
				}
			} catch (CppException e) {
				doLogging("Failed building ProjectVersion " + versionName + "!"
						+ "\nException: " + Utilities.exceptionToString(e));
			}

		} else {
			doLogging("Loading ProjectVersion from file " + inputFile + "...");
			try (final InputStream inputStream = Files.newInputStream(Path.of(inputFile), StandardOpenOption.READ)) {
				final ProjectVersion projectVersion = ProjectVersion.fromInputStream(inputStream);
				doLogging("Success loading ProjectVersion from file " + inputFile);
				return projectVersion;
			} catch (IOException e) {
				doLogging("Failed reading ProjectVersion from input file! ProjectVersion file: " + inputFile
						+ "\nException: " + Utilities.exceptionToString(e));
			}
		}
		return null;
	}

	private VersionDifference buildVersionDifference(Profile.Section section, Map<String, ProjectVersion> versionMap) {
		final String inputFile = section.get("inputFile", "");
		if (inputFile.isBlank()) {
			final ProjectVersion versionA = versionMap.get(section.get("versionA", ""));
			final ProjectVersion versionB = versionMap.get(section.get("versionB", ""));
			if (versionA == null || versionB == null) {
				doLogging("Invalid config file VersionDifference format, missing versionA=\"" + versionA + "\" or/and versionB=\"" + versionB + "\"!");
				return null;
			}

			doLogging("Building VersionDifference " + versionA.getVersionName() + "-" + versionB.getVersionName() + "...");

			try {
				final VersionDifference versionDifference = VersionDiffer.compare(versionA, versionB);

				final String outputFileString = section.get("outputFile", "");
				if (!outputFileString.isBlank()) {
					final Path outputFilePath = Path.of(outputFileString);
					try (final OutputStream outputStream = Files.newOutputStream(outputFilePath,
							StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
						versionDifference.toOutputStream(outputStream);
						doLogging("Success building VersionDifference " + versionA.getVersionName() + "-" + versionB.getVersionName());
						return versionDifference;
					} catch (IOException e) {
						doLogging("Failed writing VersionDifference to output file! Output file: " + outputFilePath.toString()
								+ "\nException: " + Utilities.exceptionToString(e));
					}
				} else {
					doLogging("Skip writing VersionDifference to output file.");
					return versionDifference;
				}
			} catch (CppException e) {
				doLogging("Failed building VersionDifference " + versionA.getVersionName() + "-" + versionB.getVersionName() + "!"
						+ "\nException: " + Utilities.exceptionToString(e));
			}

		} else {
			doLogging("Loading VersionDifference from file " + inputFile + "...");
			try (final InputStream inputStream = Files.newInputStream(Path.of(inputFile), StandardOpenOption.READ)) {
				final VersionDifference difference = VersionDifference.fromInputStream(inputStream);
				doLogging("Success loading VersionDifference from file " + inputFile);
				return difference;
			} catch (IOException e) {
				doLogging("Failed reading VersionDifference from input file! Input file: " + inputFile
						+ "\nException: " + Utilities.exceptionToString(e));
			}
		}
		return null;
	}

	private Project buildProject(Profile.Section section, List<ProjectVersion> versionList, List<VersionDifference> differenceList) {
		final String inputFile = section.get("inputFile", "");
		if (inputFile.isBlank()) {
			final String projectName = section.get("projectName", "");
			if (projectName.isBlank()) {
				doLogging("Invalid config file Project format, missing projectName=\"" + projectName + "\"!");
				return null;
			}

			doLogging("Building Project " + projectName + "...");

			final Project project = Project.of(projectName, versionList, differenceList);
			final String outputFileString = section.get("outputFile", "");
			if (!outputFileString.isBlank()) {
				final Path outputFilePath = Path.of(outputFileString);
				try (final OutputStream outputStream = Files.newOutputStream(outputFilePath,
						StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
					project.toOutputStream(outputStream);
					doLogging("Success building Project " + projectName);
					return project;
				} catch (IOException e) {
					doLogging("Failed writing Project to output file! Project file: " + outputFilePath.toString()
							+ "\nException: " + Utilities.exceptionToString(e));
				}
			} else {
				doLogging("Skip writing Project to output file.");
				return project;
			}
		} else {
			doLogging("Loading Project from file " + inputFile + "...");
			try (final InputStream inputStream = Files.newInputStream(Path.of(inputFile), StandardOpenOption.READ)) {
				final Project project = Project.fromInputStream(inputStream);
				doLogging("Success loading Project from file " + inputFile);
				return project;
			} catch (IOException e) {
				doLogging("Failed reading Project from input file! Input file: " + inputFile
						+ "\nException: " + Utilities.exceptionToString(e));
			}
		}
		return null;
	}
}
