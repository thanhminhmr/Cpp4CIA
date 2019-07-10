package mrmathami.cia.cpp;

import mrmathami.cia.cpp.ast.INode;
import mrmathami.cia.cpp.ast.IRoot;
import mrmathami.cia.cpp.builder.VersionBuilder;
import mrmathami.cia.cpp.builder.VersionBuilderDebugger;
import mrmathami.cia.cpp.database.Database;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DatabaseTest {
	private DatabaseTest() {
	}

	public static void main(String[] argv) throws IOException {
		long start_time = System.nanoTime();

		final Path projectRoot = Path.of("D:\\Research\\SourceCodeComparator\\test\\sr2");
		final List<Path> projectFiles =
				List.of(Path.of("D:\\Research\\SourceCodeComparator\\test\\sr2\\sr2.cpp"));
//				List.of(
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						Path.of("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				);
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
		debugger.setSaveTranslationUnit(true);
		debugger.setSaveRoot(true);

		final ProjectVersion projectVersion = VersionBuilder.build("CmderLauncher", projectRoot, projectFiles, includePaths, debugger);
		if (projectVersion == null) return;

		final IRoot rootNode = projectVersion.getRootNode();
		final Set<INode> changeSet = new HashSet<>();
		for (final INode node : rootNode) {
			if (node.getName().equals("t") || node.getName().equals("sm")) {
				changeSet.add(node);
			}
		}
		rootNode.calculateImpact(changeSet);

		try (final BufferedWriter fos = new BufferedWriter(new FileWriter(projectRoot.resolve("log.txt").toString()))) {
			final List<INode> nodeList = new ArrayList<>();
			nodeList.add(rootNode);
			for (final INode node : rootNode) {
				nodeList.add(node);
			}
			nodeList.sort((o1, o2) -> Float.compare(o1.getImpact(), o2.getImpact()));
			for (final INode node : nodeList) {
				fos.write(node.toString());
				fos.write("\n");
			}
		}

		debugger.debugOutput(projectRoot);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);

		try (final FileOutputStream fos = new FileOutputStream("R:\\CmderLauncher.proj")) {
			projectVersion.toOutputStream(fos);
		}

		debugger.debugOutput(projectRoot);

		System.out.println((System.nanoTime() - start_time) / 1000000.0);


		ProjectVersion projectVersion2;
		try (final FileInputStream fileInputStream = new FileInputStream("R:\\CmderLauncher.proj")) {
			projectVersion2 = ProjectVersion.fromInputStream(fileInputStream);
		}
		{
			List<INode> nodeList = new ArrayList<>();
			for (INode node : projectVersion.getRootNode()) {
				nodeList.add(node);
			}
			nodeList.sort(Comparator.comparingInt(INode::hashCode));

			List<INode> nodeList2 = new ArrayList<>();
			for (INode node : projectVersion2.getRootNode()) {
				nodeList2.add(node);
			}
			nodeList2.sort(Comparator.comparingInt(INode::hashCode));

			if (!nodeList.equals(nodeList2)) {
				System.out.println("Error");
			}

		}


		final Project project = Project.of("tesseract", List.of(projectVersion), List.of());

		Database.exportProject(project, Path.of("R:\\"));
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}
}
