package mrmathami.cia.cpp.differ;

import mrmathami.cia.cpp.ast.Node;
import mrmathami.util.Pair;
import mrmathami.util.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

public final class VersionDifferDebugger {
	private String versionDifferenceName;
	private Set<Node> addedNodes;
	private Set<Node> removedNodes;
	private Set<Pair<Node, Node>> changedNodes;
	private Set<Pair<Node, Node>> unchangedNodes;

	public String getVersionDifferenceName() {
		return versionDifferenceName;
	}

	public void setVersionDifferenceName(String versionDifferenceName) {
		this.versionDifferenceName = versionDifferenceName;
	}

	public Set<Node> getAddedNodes() {
		return addedNodes;
	}

	public void setAddedNodes(Set<Node> addedNodes) {
		this.addedNodes = addedNodes;
	}

	public Set<Node> getRemovedNodes() {
		return removedNodes;
	}

	public void setRemovedNodes(Set<Node> removedNodes) {
		this.removedNodes = removedNodes;
	}

	public Set<Pair<Node, Node>> getChangedNodes() {
		return changedNodes;
	}

	public void setChangedNodes(Set<Pair<Node, Node>> changedNodes) {
		this.changedNodes = changedNodes;
	}

	public Set<Pair<Node, Node>> getUnchangedNodes() {
		return unchangedNodes;
	}

	public void setUnchangedNodes(Set<Pair<Node, Node>> unchangedNodes) {
		this.unchangedNodes = unchangedNodes;
	}

	public void debugOutput(Path outputPath) {
		try (final FileWriter fileWriter = new FileWriter(outputPath.resolve("VersionDifference-" + versionDifferenceName + ".log").toString())) {
			fileWriter.write("\n\nAdded nodes:\n");
			fileWriter.write(Utilities.collectionToString(addedNodes));
			fileWriter.write("\n\nRemoved nodes:\n");
			fileWriter.write(Utilities.collectionToString(removedNodes));
			fileWriter.write("\n\nChanged nodes:\n");
			fileWriter.write(Utilities.collectionToString(changedNodes));
			fileWriter.write("\n\nUnchanged nodes:\n");
			fileWriter.write(Utilities.collectionToString(unchangedNodes));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
