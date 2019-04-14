package cia.cpp;

import cia.cpp.builder.VersionBuilder;
import cia.cpp.database.Database;
import cia.cpp.differ.VersionDiffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public final class DifferTest {
	private DifferTest() {
	}

	public static void main(String[] args) throws IOException {
		//System.in.read();
		long start_time = System.nanoTime();
		{
			final List<File> projectFiles =
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//				BuilderTest.readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
					List.of(
							new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\main.cpp"),
							new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.cpp"),
							new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.0\\TinyEXIF.h")
					);
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Array.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Bitmap.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Buffer.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\NeuralNetwork.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Pixel.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Randomizer.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Trainer.cpp")
//				);

			final List<File> includePaths = List.of();
			final ProjectVersion projectVersion = VersionBuilder.build("project1", projectFiles, includePaths, false);

			if (projectVersion == null) return;

			System.out.println((System.nanoTime() - start_time) / 1000000.0);


			final List<File> projectFiles2 =
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//				BuilderTest.readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
					List.of(
							new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\main.cpp"),
							new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.cpp"),
							new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.h")
					);
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Array.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Bitmap.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Buffer.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\NeuralNetwork.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Pixel.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Randomizer.h"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\meo_nn\\Trainer.cpp")
//				);

			final List<File> includePaths2 = List.of();
			final ProjectVersion projectVersion2 = VersionBuilder.build("project2", projectFiles2, includePaths2, false);

			if (projectVersion2 == null) return;


			try (final FileOutputStream fos = new FileOutputStream("R:\\project1.proj")) {
				projectVersion.toOutputStream(fos);
			}
			try (final FileOutputStream fos = new FileOutputStream("R:\\project2.proj")) {
				projectVersion2.toOutputStream(fos);
			}
		}

		ProjectVersion projectVersion, projectVersion2;

		try (final FileInputStream fileInputStream = new FileInputStream("R:\\project1.proj")) {
			projectVersion = ProjectVersion.fromInputStream(fileInputStream);
		}
		try (final FileInputStream fileInputStream = new FileInputStream("R:\\project2.proj")) {
			projectVersion2 = ProjectVersion.fromInputStream(fileInputStream);
		}

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final VersionDifference difference = VersionDiffer.compare(projectVersion, projectVersion2);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);
		try (final FileOutputStream fos = new FileOutputStream("R:\\project_project2.pcmp")) {
			difference.toOutputStream(fos);
		}
		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		final Project project = Project.of("project", List.of(projectVersion, projectVersion2), List.of(difference));

		Database.exportProject(project, new File("R:\\"));
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
