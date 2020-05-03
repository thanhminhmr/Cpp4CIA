package mrmathami.cia.cpp;

import mrmathami.cia.cpp.builder.ProjectVersion;
import mrmathami.cia.cpp.differ.VersionDifference;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

public final class Project implements Serializable {
	private static final long serialVersionUID = 4397888814551456285L;

	@Nonnull private final String projectName;
	@Nonnull private final List<ProjectVersion> versionList;
	@Nonnull private final List<VersionDifference> differenceList;

	public Project(@Nonnull String projectName, @Nonnull List<ProjectVersion> versionList, @Nonnull List<VersionDifference> differenceList) {
		this.projectName = projectName;
		this.versionList = List.copyOf(versionList);
		this.differenceList = List.copyOf(differenceList);
	}

	@Nonnull
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
	public final String getProjectName() {
		return projectName;
	}

	@Nonnull
	public final List<ProjectVersion> getVersionList() {
		return versionList;
	}

	@Nonnull
	public final List<VersionDifference> getDifferenceList() {
		return differenceList;
	}
}
