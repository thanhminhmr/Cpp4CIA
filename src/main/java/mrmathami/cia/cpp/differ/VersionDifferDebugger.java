package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.ProjectVersion;
import mrmathami.cia.cpp.ast.Node;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.util.Pair;
import mrmathami.util.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public final class VersionDifferDebugger {
	private VersionDifferDebugger() {
	}

	public static void debugOutput(Path outputPath, ProjectVersion projectVersionA, ProjectVersion projectVersionB,
			Set<Node> addedNodes, Set<Node> removedNodes, Set<Pair<Node, Node>> changedNodes,
			Set<Pair<Node, Node>> unchangedNodes, Map<Node, Float> impactWeights) {

		try (final FileWriter fileWriter = new FileWriter(outputPath.resolve("VersionDifference-"
				+ projectVersionA.getVersionName() + "-" + projectVersionB.getVersionName() + ".log").toString())) {
			fileWriter.write("\n\nAdded nodes:\n");
			fileWriter.write(Utilities.collectionToString(addedNodes));
			fileWriter.write("\n\nRemoved nodes:\n");
			fileWriter.write(Utilities.collectionToString(removedNodes));
			fileWriter.write("\n\nChanged nodes:\n");
			fileWriter.write(Utilities.collectionToString(changedNodes));
			fileWriter.write("\n\nUnchanged nodes:\n");
			fileWriter.write(Utilities.collectionToString(unchangedNodes));
			fileWriter.write("\n\nImpact weights:\n");
			for (final Map.Entry<Node, Float> entry : impactWeights.entrySet()) {
				fileWriter.write(entry.getValue() + ", " + entry.getKey().toString() + "\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
