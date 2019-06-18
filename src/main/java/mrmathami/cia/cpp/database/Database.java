package mrmathami.cia.cpp.database;

import mrmathami.cia.cpp.Project;
import mrmathami.cia.cpp.ProjectVersion;
import mrmathami.cia.cpp.VersionDifference;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.IClass;
import mrmathami.cia.cpp.ast.IEnum;
import mrmathami.cia.cpp.ast.IFunction;
import mrmathami.cia.cpp.ast.IIntegral;
import mrmathami.cia.cpp.ast.INamespace;
import mrmathami.cia.cpp.ast.INode;
import mrmathami.cia.cpp.ast.IRoot;
import mrmathami.cia.cpp.ast.ITypeContainer;
import mrmathami.cia.cpp.ast.IVariable;
import mrmathami.cia.cpp.database.bean.Base;
import mrmathami.cia.cpp.database.bean.Difference;
import mrmathami.cia.cpp.database.bean.Node;
import mrmathami.cia.cpp.database.bean.Parameter;
import mrmathami.cia.cpp.database.bean.Use;
import mrmathami.cia.cpp.database.bean.Version;
import mrmathami.cia.cpp.database.dao.Bases;
import mrmathami.cia.cpp.database.dao.Differences;
import mrmathami.cia.cpp.database.dao.Nodes;
import mrmathami.cia.cpp.database.dao.Parameters;
import mrmathami.cia.cpp.database.dao.Tables;
import mrmathami.cia.cpp.database.dao.Uses;
import mrmathami.cia.cpp.database.dao.Versions;
import mrmathami.util.Utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Database {
	private static final String DRIVER = "org.sqlite.JDBC";
	private static final String DATABASE_URL_PREFIX = "jdbc:sqlite:";

	static {
		try {
			Class.forName(DRIVER);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private final Project project;
	private final Map<Map.Entry<INode, Integer>, Node> nodeMap = new HashMap<>();
	private final Map<ProjectVersion, Version> versionMap = new HashMap<>();
	private final Connection connection;

	private Database(Project project, Path outputPath) throws IOException, SQLException {
		this.project = project;

		if (Files.isDirectory(outputPath)) {
			final Path outputFile = outputPath.resolve(project.getProjectName() + ".sqLite");
			if (Files.exists(outputFile)) Files.delete(outputFile);

			this.connection = DriverManager.getConnection(DATABASE_URL_PREFIX + outputFile.toUri().toString());
		} else {
			if (Files.exists(outputPath)) Files.delete(outputPath);

			this.connection = DriverManager.getConnection(DATABASE_URL_PREFIX + outputPath.toUri().toString());
		}
	}

	private static String pathListToString(List<String> pathList) {
		final StringBuilder builder = new StringBuilder();
		for (final String path : pathList) {
			if (builder.length() > 0) builder.append('\n');
			builder.append(path);
		}
		return builder.toString();
	}

	public static boolean exportProject(Project project, Path outputPath) {
		try {
			final Database database = new Database(project, outputPath);
			return database.internalExportProject();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static int getNodeType(INode node) {
		if (node instanceof IIntegral) {
			return 0;
		} else if (node instanceof IClass) {
			return 1;
		} else if (node instanceof IEnum) {
			return 2;
		} else if (node instanceof IFunction) {
			return 3;
		} else if (node instanceof INamespace) {
			return 4;
		} else if (node instanceof IRoot) {
			return 5;
		} else if (node instanceof IVariable) {
			return 6;
		}
		throw new IllegalStateException(Utilities.objectToString(node));
	}

	private static int getDependencyType(DependencyType type) {
		return type.ordinal(); // TODO: fix me
	}

	private Connection getConnection() {
		return connection;
	}

	@SuppressWarnings("Duplicates")
	private boolean internalExportProject() throws SQLException {
		Tables.create(getConnection());

		final List<ProjectVersion> versionList = project.getVersionList();
		for (final ProjectVersion version : versionList) {
			if (internalExportVersion(version) == null) return false;
		}

		final List<VersionDifference> differenceList = project.getDifferenceList();
		for (final VersionDifference difference : differenceList) {
			final ProjectVersion versionA = difference.getVersionA();
			final ProjectVersion versionB = difference.getVersionB();

			final Version dbVersionA = internalExportVersion(versionA);
			if (dbVersionA == null) return false;

			final Version dbVersionB = internalExportVersion(versionB);
			if (dbVersionB == null) return false;

			for (final Map.Entry<INode, INode> unchangedPair : difference.getUnchangedNodes()) {
				final INode nodeA = unchangedPair.getKey();
				final INode nodeB = unchangedPair.getValue();

				final Node dbNodeA = internalExportNode(dbVersionA, nodeA);
				if (dbNodeA == null) return false;

				final Node dbNodeB = internalExportNode(dbVersionB, nodeB);
				if (dbNodeB == null) return false;

				final Difference dbDifference = Differences.add(getConnection(), new Difference()
						.setVersionA(dbVersionA.getId())
						.setVersionB(dbVersionB.getId())
						.setNodeA(dbNodeA.getId())
						.setNodeB(dbNodeB.getId())
						.setTypeEnum(0) // unchanged
				);
				if (dbDifference == null) return false;
			}
			for (final Map.Entry<INode, INode> changedPair : difference.getChangedNodes()) {
				//noinspection Duplicates
				final INode nodeA = changedPair.getKey();
				final INode nodeB = changedPair.getValue();

				final Node dbNodeA = internalExportNode(dbVersionA, nodeA);
				if (dbNodeA == null) return false;

				final Node dbNodeB = internalExportNode(dbVersionB, nodeB);
				if (dbNodeB == null) return false;

				final Difference dbDifference = Differences.add(getConnection(), new Difference()
						.setVersionA(dbVersionA.getId())
						.setVersionB(dbVersionB.getId())
						.setNodeA(dbNodeA.getId())
						.setNodeB(dbNodeB.getId())
						.setTypeEnum(1) // changed
				);
				if (dbDifference == null) return false;
			}
			for (final INode nodeB : difference.getAddedNodes()) {
				final Node dbNodeB = internalExportNode(dbVersionB, nodeB);
				if (dbNodeB == null) return false;

				final Difference dbDifference = Differences.add(getConnection(), new Difference()
						.setVersionA(dbVersionA.getId())
						.setVersionB(dbVersionB.getId())
						//.setNodeA(null)
						.setNodeB(dbNodeB.getId())
						.setTypeEnum(2) // added
				);
				if (dbDifference == null) return false;
			}
			for (final INode nodeA : difference.getRemovedNodes()) {
				final Node dbNodeA = internalExportNode(dbVersionA, nodeA);
				if (dbNodeA == null) return false;

				final Difference dbDifference = Differences.add(getConnection(), new Difference()
						.setVersionA(dbVersionA.getId())
						.setVersionB(dbVersionB.getId())
						.setNodeA(dbNodeA.getId())
						//.setNodeB(null)
						.setTypeEnum(3) // removed
				);
				if (dbDifference == null) return false;
			}
		}
		return true;
	}

	private Version internalExportVersion(ProjectVersion version) throws SQLException {
		if (version == null) return null;
		final Version dbVersionFromMap = versionMap.get(version);
		if (dbVersionFromMap != null) return dbVersionFromMap;

		final Version dbVersion = Versions.add(getConnection(), new Version()
				.setName(version.getVersionName())
				.setFiles(pathListToString(version.getProjectFiles()))
				.setPaths(pathListToString(version.getIncludePaths())));
		if (dbVersion == null) return null;

		versionMap.put(version, dbVersion);

		final Node dbRootNode = internalExportNode(dbVersion, version.getRootNode());
		if (dbRootNode == null || Versions.update(getConnection(),
				new Version().setId(dbVersion.getId()), dbVersion.setRootId(dbRootNode.getId())) == null) {
			return null;
		}
		return dbVersion;
	}

	private Node internalExportNode(Version dbVersion, INode node) throws SQLException {
		if (node == null) return null;

		final Node dbNodeFromMap = nodeMap.get(Map.entry(node, System.identityHashCode(node)));
		if (dbNodeFromMap != null) return dbNodeFromMap;

		final Node dbParentNode = internalExportNode(dbVersion, node.getParent());
		if (dbParentNode == null && !(node instanceof IRoot)) return null;

		final Node dbTypeNode = node instanceof ITypeContainer
				? internalExportNode(dbVersion, ((ITypeContainer) node).getType())
				: null;

		final Node dbNode = Nodes.add(getConnection(), new Node()
				.setTypeEnum(getNodeType(node))
				.setParentId(dbParentNode != null ? dbParentNode.getId() : null)
				.setName(node.getName())
				.setUniqueName(node.getUniqueName())
				.setSignature(node.getSignature())
				.setTypeId(dbTypeNode != null ? dbTypeNode.getId() : null)
				.setVersionId(dbVersion.getId()));
		if (dbNode == null) return null;

		nodeMap.put(Map.entry(node, System.identityHashCode(node)), dbNode);

		if (node instanceof IClass) {
			for (final INode baseNode : ((IClass) node).getBases()) {
				final Node dbBaseNode = internalExportNode(dbVersion, baseNode);
				if (dbBaseNode == null) return null;

				final Base dbBase = Bases.add(getConnection(), new Base()
						.setClassId(dbNode.getId())
						.setBaseId(dbBaseNode.getId()));
				if (dbBase == null) return null;
			}
		} else if (node instanceof IFunction) {
			for (final INode parameterNode : ((IFunction) node).getParameters()) {
				final Node dbParameterNode = internalExportNode(dbVersion, parameterNode);
				if (dbParameterNode == null) return null;

				final Parameter dbParameter = Parameters.add(getConnection(), new Parameter()
						.setFunctionId(dbNode.getId())
						.setParameterId(dbParameterNode.getId()));
				if (dbParameter == null) return null;
			}
		}

		for (final INode dependencyNode : node.getAllDependencyTo()) {
			final Node dbUseNode = internalExportNode(dbVersion, dependencyNode);
			if (dbUseNode == null)
				return null;

			for (final Map.Entry<DependencyType, Integer> entry : node.getNodeDependencyFrom(dependencyNode).entrySet()) {
				final int count = entry.getValue();
				if (count == 0) continue;

				final Use dbUse = Uses.add(getConnection(), new Use()
						.setVersionId(dbVersion.getId())
						.setNodeA(dbNode.getId())
						.setNodeB(dbUseNode.getId())
						.setTypeEnum(getDependencyType(entry.getKey()))
						.setCount(count));
				if (dbUse == null)
					return null;
			}
		}

		for (final INode childNode : node) {
			if (internalExportNode(dbVersion, childNode) == null)
				return null;
		}

		return dbNode;
	}
}
