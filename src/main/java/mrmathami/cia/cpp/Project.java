package mrmathami.cia.cpp;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

public final class Project implements Serializable {
	private static final long serialVersionUID = 6617345373264477890L;

	private final String projectName;
	private final List<ProjectVersion> versionList;
	private final List<VersionDifference> differenceList;

	private Project(String projectName, List<ProjectVersion> versionList, List<VersionDifference> differenceList) {
		this.projectName = projectName;
		this.versionList = List.copyOf(versionList);
		this.differenceList = List.copyOf(differenceList);
	}

	public static Project of(String projectName, List<ProjectVersion> versionList, List<VersionDifference> differenceList) {
		return new Project(projectName, versionList, differenceList);
	}

	public static Project fromInputStream(InputStream inputStream) throws IOException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try {
			return (Project) objectInputStream.readObject();
		} catch (ClassNotFoundException | ClassCastException e) {
			throw new IOException("Wrong input file format!", e);
		}
	}

	public final void toOutputStream(OutputStream outputStream) throws IOException {
		final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
		objectOutputStream.writeObject(this);
		objectOutputStream.flush();
	}

	public final String getProjectName() {
		return projectName;
	}

	public final List<ProjectVersion> getVersionList() {
		return versionList;
	}

	public final List<VersionDifference> getDifferenceList() {
		return differenceList;
	}
}
