package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.ast.RootNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ProjectVersion implements Serializable {
	private static final long serialVersionUID = 7212810535184666471L;

	@Nonnull private final String versionName;
	@Nonnull private final List<String> projectFiles;
	@Nonnull private final List<String> includePaths;
	@Nonnull private final RootNode rootNode;
	@Nonnull private final double[] typeWeights;
	@Nonnull private final double[] weights;

	@Nullable private transient Map<DependencyType, Double> typeWeightMap;
	@Nullable private transient Map<CppNode, Double> weightMap;

	ProjectVersion(@Nonnull String versionName, @Nonnull List<String> projectFiles,
			@Nonnull List<String> includePaths, @Nonnull RootNode rootNode,
			@Nonnull double[] typeWeights, @Nonnull double[] weights) {
		this.versionName = versionName;
		this.projectFiles = List.copyOf(projectFiles);
		this.includePaths = List.copyOf(includePaths);
		this.rootNode = rootNode;
		this.typeWeights = typeWeights.clone();
		this.weights = weights.clone();
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
	public final Map<DependencyType, Double> getDependencyTypeWeightMap() {
		if (typeWeightMap != null) return typeWeightMap;
		final Map<DependencyType, Double> map = new EnumMap<>(DependencyType.class);
		for (final DependencyType type : DependencyType.values()) map.put(type, typeWeights[type.ordinal()]);
		return this.typeWeightMap = Map.copyOf(map);
	}

	@Nonnull
	public final Map<CppNode, Double> getWeightMap() {
		if (weightMap != null) return weightMap;
		final Map<CppNode, Double> map = new IdentityHashMap<>();
		map.put(rootNode, weights[0]); // root id == 0
		for (final CppNode node : rootNode) map.put(node, weights[node.getId()]);
		return this.weightMap = Map.copyOf(map);
	}
}
