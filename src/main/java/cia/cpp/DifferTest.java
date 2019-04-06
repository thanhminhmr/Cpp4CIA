package cia.cpp;

import cia.cpp.builder.ProjectBuilder;
import cia.cpp.differ.ProjectDiffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public final class DifferTest {
	private DifferTest() {
	}

	public static void main(String[] args) throws Exception {
		//System.in.read();
		long start_time = System.nanoTime();
		{
			final List<File> projectFiles =
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//				readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
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
			final Project project = ProjectBuilder.build("project1", projectFiles, includePaths, false);

			if (project == null) return;

			System.out.println((System.nanoTime() - start_time) / 1000000.0);


			final List<File> projectFiles2 =
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
//				readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"));
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
			final Project project2 = ProjectBuilder.build("project2", projectFiles2, includePaths2, false);

			if (project2 == null) return;


			try (final FileOutputStream fos = new FileOutputStream("R:\\project1.proj")) {
				project.toOutputStream(fos);
			}
			try (final FileOutputStream fos = new FileOutputStream("R:\\project2.proj")) {
				project2.toOutputStream(fos);
			}
		}


		Project project, project2;

		try (final FileInputStream fileInputStream = new FileInputStream("R:\\project1.proj")) {
			project = Project.fromInputStream(fileInputStream);
		}
		try (final FileInputStream fileInputStream = new FileInputStream("R:\\project2.proj")) {
			project2 = Project.fromInputStream(fileInputStream);
		}

		System.out.println((System.nanoTime() - start_time) / 1000000.0);


		ProjectDiffer.compare(project, project2);


		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
