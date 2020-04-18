package mrmathami.cia.cpp;

import mrmathami.cia.cpp.ast.Node;
import mrmathami.cia.cpp.ast.RootNode;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ProjectVersion implements Serializable {
	private static final long serialVersionUID = -8512661157548869855L;

	@Nonnull private final String versionName;
	@Nonnull private final List<String> projectFiles;
	@Nonnull private final List<String> includePaths;
	@Nonnull private final RootNode rootNode;
	@Nonnull private final double[] weights;

	private ProjectVersion(@Nonnull String versionName, @Nonnull List<String> projectFiles,
			@Nonnull List<String> includePaths, @Nonnull RootNode rootNode, @Nonnull double[] weights) {
		this.versionName = versionName;
		this.projectFiles = List.copyOf(projectFiles);
		this.includePaths = List.copyOf(includePaths);
		this.rootNode = rootNode;
		this.weights = weights.clone();
	}

	@Nonnull
	public static ProjectVersion of(@Nonnull String versionName, @Nonnull List<String> projectFiles,
			@Nonnull List<String> includePaths, @Nonnull RootNode rootNode, @Nonnull double[] weights) {
		return new ProjectVersion(versionName, projectFiles, includePaths, rootNode, weights);
	}

	@Nonnull
	public static ProjectVersion fromInputStream(@Nonnull InputStream inputStream) throws IOException {
		final ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		try {
			return (ProjectVersion) objectInputStream.readObject();
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
	public final String getVersionName() {
		return versionName;
	}

	@Nonnull
	public final List<String> getProjectFiles() {
		return projectFiles;
	}

	@Nonnull
	public final List<String> getIncludePaths() {
		return includePaths;
	}

	@Nonnull
	public final RootNode getRootNode() {
		return rootNode;
	}

	@Nonnull
	public final Map<Node, Double> getWeightMap() {
		final Map<Node, Double> map = new IdentityHashMap<>();
		map.put(rootNode, weights[0]); // root id == 0
		for (final Node node : rootNode) {
			map.put(node, weights[node.getId()]);
		}
		return map;
	}
}
