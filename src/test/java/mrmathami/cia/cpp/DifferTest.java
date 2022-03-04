package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.ProjectVersion;
import mrmathami.cia.cpp.differ.VersionDiffer;
import mrmathami.cia.cpp.differ.VersionDifference;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class DifferTest {
	private DifferTest() {
	}

	public static void main(String[] args) throws IOException, CppException {
//		System.out.println("press enter to start!");
//		System.in.read();
		final long start_time = System.nanoTime();

		final Path oldPath = Path.of("test4_old.proj");
		final Path newPath = Path.of("test4_new.proj");

		try (final InputStream oldPathInputStream = Files.newInputStream(oldPath);
				final InputStream newPathInputStream = Files.newInputStream(newPath)) {
			final ProjectVersion projectVersion = ProjectVersion.fromInputStream(oldPathInputStream);
			final ProjectVersion projectVersion2 = ProjectVersion.fromInputStream(newPathInputStream);

			System.out.println((System.nanoTime() - start_time) / 1000000.0);

			final VersionDifference difference = VersionDiffer.compare(projectVersion, projectVersion2,
					VersionDiffer.IMPACT_WEIGHT_MAP, 8);

			System.out.println((System.nanoTime() - start_time) / 1000000.0);
			try (final FileOutputStream fos = new FileOutputStream("project1_project2.VersionDifference")) {
				difference.toOutputStream(fos);
				System.out.println((System.nanoTime() - start_time) / 1000000.0);
			}
		}

//		final Project project = new Project("project", List.of(projectVersion, projectVersion2), List.of(difference));
//		try (final FileOutputStream fos = new FileOutputStream("./test/project.Project")) {
//			project.toOutputStream(fos);
//		}
//		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
