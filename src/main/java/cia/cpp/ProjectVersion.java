package cia.cpp;

import cia.cpp.ast.IRoot;

import java.io.*;
import java.util.List;

public final class ProjectVersion implements Serializable {
	private static final long serialVersionUID = 8469860416876982231L;

	private final String versionName;
	private final List<File> projectFiles;
	private final List<File> includePaths;
	private final IRoot rootNode;

	private ProjectVersion(String versionName, List<File> projectFiles, List<File> includePaths, IRoot rootNode) {
		this.versionName = versionName;
		this.projectFiles = projectFiles;
		this.includePaths = includePaths;
		this.rootNode = rootNode;
	}

	public static ProjectVersion of(String projectName, List<File> projectFiles, List<File> includePaths, IRoot rootNode) {
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

	public final List<File> getProjectFiles() {
		return projectFiles;
	}

	public final List<File> getIncludePaths() {
		return includePaths;
	}

	public final IRoot getRootNode() {
		return rootNode;
	}
}
