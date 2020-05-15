package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.builder.ProjectVersion;
import mrmathami.util.Pair;
import mrmathami.util.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class VersionDifferDebugger {
	private VersionDifferDebugger() {
	}

	public static void debugOutput(Path outputPath, VersionDifference versionDifference) {
		final ProjectVersion projectVersionA = versionDifference.getVersionA();
		final ProjectVersion projectVersionB = versionDifference.getVersionB();
		final Set<CppNode> addedNodes = versionDifference.getAddedNodes();
		final Set<CppNode> removedNodes = versionDifference.getRemovedNodes();
		final Set<Pair<CppNode, CppNode>> changedNodes = versionDifference.getChangedNodes();
		final Set<Pair<CppNode, CppNode>> unchangedNodes = versionDifference.getUnchangedNodes();
		final Map<CppNode, Double> impactWeightMap = versionDifference.getImpactWeightMap();

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

			final List<Pair<CppNode, Double>> list = new ArrayList<>(impactWeightMap.size());
			for (final Map.Entry<CppNode, Double> entry : impactWeightMap.entrySet()) {
				list.add(Pair.immutableOf(entry.getKey(), entry.getValue()));
			}
			list.sort((o1, o2) -> {
				final int compare = Double.compare(o2.getB(), o1.getB());
				return compare != 0 ? compare : Integer.compare(o1.getA().getId(), o2.getA().getId());
			});
			for (final Pair<CppNode, Double> pair : list) {
				fileWriter.write(String.format("%.8f, %s\n", pair.getB(), pair.getA()));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
