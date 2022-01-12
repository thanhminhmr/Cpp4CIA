package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.ProjectVersion;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SerializerTest {
	private SerializerTest() {
	}

	public static void main(String[] args) throws Exception {
//		System.in.read();
		long start_time = System.nanoTime();
		{
			final Path outputProject = Path.of("PrusaSlicer_old_2.proj");
			try (final InputStream inputStream = Files.newInputStream(outputProject)) {
				final ProjectVersion projectVersion = ProjectVersion.fromInputStream(inputStream);
				System.out.println(projectVersion.getRootNode().toTreeString());
				System.out.println(projectVersion);
			}

			System.out.println((System.nanoTime() - start_time) / 1000000.0);
		}

//		final VersionDifference difference = VersionDiffer.compare(projectVersion, projectVersion2, VersionDiffer.IMPACT_WEIGHT_MAP);
//
//		System.out.println((System.nanoTime() - start_time) / 1000000.0);
//		try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\project1_project2.VersionDifference")) {
//			difference.toOutputStream(fos);
//		}
//		System.out.println((System.nanoTime() - start_time) / 1000000.0);
//
//		final Project project = new Project("project", List.of(projectVersion, projectVersion2), List.of(difference));
//		try (final FileOutputStream fos = new FileOutputStream("C:\\WINDOWS\\TEMP\\Temp\\project.Project")) {
//			project.toOutputStream(fos);
//		}
//		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
