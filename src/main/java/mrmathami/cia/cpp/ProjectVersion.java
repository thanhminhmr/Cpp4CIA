package mrmathami.cia.cpp;

import mrmathami.cia.cpp.ast.IRoot;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

public final class ProjectVersion implements Serializable {
	private static final long serialVersionUID = 3690872070049507458L;

	private final String versionName;
	private final List<String> projectFiles;
	private final List<String> includePaths;
	private final IRoot rootNode;

	private ProjectVersion(String versionName, List<String> projectFiles, List<String> includePaths, IRoot rootNode) {
		this.versionName = versionName;
		this.projectFiles = List.copyOf(projectFiles);
		this.includePaths = List.copyOf(includePaths);
		this.rootNode = rootNode;
	}

	public static ProjectVersion of(String projectName, List<String> projectFiles, List<String> includePaths, IRoot rootNode) {
		return new ProjectVersion(projectName, projectFiles, includePaths, rootNode);
	}

	public static ProjectVersion fromInputStream(InputStream inputStream) throws IOException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try {
			return (ProjectVersion) objectInputStream.readObject();
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new IOException("Wrong input file format!", e);
		}
	}

	public final void toOutputStream(OutputStream outputStream) throws IOException {
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
	}

	public final String getVersionName() {
		return versionName;
	}

	public final List<String> getProjectFiles() {
		return projectFiles;
	}

	public final List<String> getIncludePaths() {
		return includePaths;
	}

	public final IRoot getRootNode() {
		return rootNode;
	}
}
