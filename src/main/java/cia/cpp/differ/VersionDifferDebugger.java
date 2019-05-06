package cia.cpp.differ;

import cia.cpp.ast.INode;
import mrmathami.util.Utilities;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public class VersionDifferDebugger {
	private String versionDifferenceName;
	private Set<INode> addedNodes;
	private Set<INode> removedNodes;
	private Set<Map.Entry<INode, INode>> changedNodes;
	private Set<Map.Entry<INode, INode>> unchangedNodes;

	public String getVersionDifferenceName() {
		return versionDifferenceName;
	}

	public void setVersionDifferenceName(String versionDifferenceName) {
		this.versionDifferenceName = versionDifferenceName;
	}

	public Set<INode> getAddedNodes() {
		return addedNodes;
	}

	public void setAddedNodes(Set<INode> addedNodes) {
		this.addedNodes = addedNodes;
	}

	public Set<INode> getRemovedNodes() {
		return removedNodes;
	}

	public void setRemovedNodes(Set<INode> removedNodes) {
		this.removedNodes = removedNodes;
	}

	public Set<Map.Entry<INode, INode>> getChangedNodes() {
		return changedNodes;
	}

	public void setChangedNodes(Set<Map.Entry<INode, INode>> changedNodes) {
		this.changedNodes = changedNodes;
	}

	public Set<Map.Entry<INode, INode>> getUnchangedNodes() {
		return unchangedNodes;
	}

	public void setUnchangedNodes(Set<Map.Entry<INode, INode>> unchangedNodes) {
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
