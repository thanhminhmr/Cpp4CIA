package cia.cpp;

import mrmathami.util.ImmutablePair;

import java.io.*;
import java.util.Collections;
import java.util.Map;

public final class Project implements Serializable {
	private static final long serialVersionUID = -2746772526823166486L;

	private final String projectName;
	private final Map<String, Version> versions;
	private final Map<ImmutablePair<Version, Version>, VersionDifference> differences;

	private Project(String projectName, Map<String, Version> versions, Map<ImmutablePair<Version, Version>, VersionDifference> differences) {
		this.projectName = projectName;
		this.versions = versions;
		this.differences = differences;
	}

	public static Project of(String projectName, Map<String, Version> versionList, Map<ImmutablePair<Version, Version>, VersionDifference> differenceList) {
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

	public final Map<String, Version> getVersions() {
		return Collections.unmodifiableMap(versions);
	}

	public final Map<ImmutablePair<Version, Version>, VersionDifference> getDifferences() {
		return Collections.unmodifiableMap(differences);
	}
}
