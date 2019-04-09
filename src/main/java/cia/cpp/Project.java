package cia.cpp;

import cia.cpp.ast.IRoot;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.List;

public final class Project implements Serializable {
	private static final long serialVersionUID = -9079954477430291810L;

	@Nonnull
	private final String projectName;

	@Nonnull
	private final List<File> projectFiles;

	@Nonnull
	private final List<File> includePaths;

	@Nonnull
	private final IRoot rootNode;

	private Project(@Nonnull String projectName, @Nonnull List<File> projectFiles, @Nonnull List<File> includePaths, @Nonnull IRoot rootNode) {
		this.projectName = projectName;
		this.projectFiles = projectFiles;
		this.includePaths = includePaths;
		this.rootNode = rootNode;
	}

	public static Project of(@Nonnull String projectName, @Nonnull List<File> projectFiles, @Nonnull List<File> includePaths, @Nonnull IRoot rootNode) {
		return new Project(projectName, projectFiles, includePaths, rootNode);
	}

	public static Project fromInputStream(@Nonnull InputStream inputStream) throws IOException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try {
			return (Project) objectInputStream.readObject();
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new IOException("Wrong input file format!", e);
		}
	}

	public final void toOutputStream(@Nonnull OutputStream outputStream) throws IOException {
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
	}

	@Nonnull
	public String getProjectName() {
		return projectName;
	}

	@Nonnull
	public List<File> getProjectFiles() {
		return projectFiles;
	}

	@Nonnull
	public List<File> getIncludePaths() {
		return includePaths;
	}

	@Nonnull
	public IRoot getRootNode() {
		return rootNode;
	}
}
