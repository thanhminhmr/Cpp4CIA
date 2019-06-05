package mrmathami.cia.cpp.database.dao;

import mrmathami.cia.cpp.database.bean.BeanClass;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Tables {
	private Tables() {
	}

	private static <E extends BeanClass> PreparedStatement prepareStatement(String sqlStatement, Connection connection, E dataBean, E selectionBean) throws SQLException {
		final PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
		int parameterIndex = 0;
		if (dataBean != null) {
			for (final Object value : dataBean.values()) {
				parameterIndex += 1;
				preparedStatement.setObject(parameterIndex, value);
			}
		}
		if (selectionBean != null) {
			for (final Object value : selectionBean.values()) {
				parameterIndex += 1;
				preparedStatement.setObject(parameterIndex, value);
			}
		}
		return preparedStatement;
	}

	public static <E extends BeanClass> List<E> select(Connection connection, Supplier<E> supplier, E selectionBean) throws SQLException {
		final StringBuilder sqlStatement = new StringBuilder()
				.append("SELECT `")
				.append(String.join("`, `", selectionBean.getFieldNames()))
				.append("` FROM `")
				.append(selectionBean.getTableName())
				.append('`');
		if (!selectionBean.isEmpty()) {
			sqlStatement.append(" WHERE `")
					.append(String.join("` = ? AND `", selectionBean.keySet()))
					.append("` = ?");
		}

		try (final PreparedStatement preparedStatement = prepareStatement(sqlStatement.toString(), connection, null, selectionBean)) {
			final ResultSet resultSet = preparedStatement.executeQuery();
			final List<E> resultList = new ArrayList<>();
			while (resultSet.next()) {
				final E resultBean = supplier.get();
				for (final String fieldName : selectionBean.getFieldNames()) {
					resultBean.put(fieldName, resultSet.getObject(fieldName));
				}
				resultList.add(resultBean);
			}
			return resultList;
		}
	}

	public static <E extends BeanClass> E insert(Connection connection, Supplier<E> supplier, E dataBean, String primaryKeyField) throws SQLException {
		final StringBuilder sqlStatement = new StringBuilder()
				.append("INSERT INTO `")
				.append(dataBean.getTableName())
				.append("` ");

		if (!dataBean.isEmpty()) {
			sqlStatement.append("(`")
					.append(String.join("`, `", dataBean.keySet()))
					.append("`) VALUES (?")
					.append(", ?".repeat(dataBean.size() - 1))
					.append(')');
		} else {
			sqlStatement.append("DEFAULT VALUES");
		}

		try (final PreparedStatement preparedStatement = prepareStatement(sqlStatement.toString(), connection, dataBean, null)) {
			if (preparedStatement.executeUpdate() != 0) {
				final ResultSet resultSet = preparedStatement.getGeneratedKeys();
				if (resultSet.next()) {
					final E resultBean = supplier.get();
					for (final String fieldName : dataBean.getFieldNames()) {
						if (fieldName.equals(primaryKeyField)) {
							resultBean.put(fieldName, resultSet.getObject(1));
						} else {
							resultBean.put(fieldName, dataBean.get(fieldName));
						}
					}
					return resultBean;
				}
			}
		}
		return null;
	}

	public static <E extends BeanClass> E update(Connection connection, Supplier<E> supplier, E dataBean, E selectionBean) throws SQLException {
		if (dataBean.isEmpty()) return null;

		final StringBuilder sqlStatement = new StringBuilder()
				.append("UPDATE `")
				.append(dataBean.getTableName())
				.append("` SET `")
				.append(String.join("` = ?, `", dataBean.keySet()))
				.append("` = ?");

		if (!selectionBean.isEmpty()) {
			sqlStatement.append(" WHERE `")
					.append(String.join("` = ? AND `", selectionBean.keySet()))
					.append("` = ?");
		}

		try (PreparedStatement preparedStatement = prepareStatement(sqlStatement.toString(), connection, dataBean, selectionBean)) {
			if (preparedStatement.executeUpdate() == 0) return null;
		}

		final E resultBean = supplier.get();
		for (final String fieldName : dataBean.getFieldNames()) {
			final Object value = dataBean.get(fieldName);
			resultBean.put(fieldName, value != null ? value : selectionBean.get(fieldName));
		}
		return resultBean;
	}

	public static <E extends BeanClass> int delete(Connection connection, E selectionBean) throws SQLException {
		final StringBuilder sqlStatement = new StringBuilder()
				.append("DELETE FROM `")
				.append(selectionBean.getTableName())
				.append('`');
		if (!selectionBean.isEmpty()) {
			sqlStatement.append(" WHERE `")
					.append(String.join("` = ? AND `", selectionBean.keySet()))
					.append("` = ?");
		}

		try (final PreparedStatement preparedStatement = prepareStatement(sqlStatement.toString(), connection, null, selectionBean)) {
			return preparedStatement.executeUpdate();
		}
	}

	public static void create(Connection connection) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			statement.execute("PRAGMA synchronous = OFF;");
			statement.execute("PRAGMA journal_mode = OFF;");
			statement.execute("PRAGMA locking_mode = EXCLUSIVE;");
			statement.execute("PRAGMA temp_store = MEMORY;");
			statement.addBatch("create table `nodes` (\n" +
					"\t`id` integer primary key autoincrement,\n" +
					"\t`typeEnum` integer not null,\n" +
					"\t`parentId` integer default null references `node` (`id`),\n" +
					"\t`name` text not null,\n" +
					"\t`uniqueName` text not null,\n" +
					"\t`signature` text not null,\n" +
					"\t`typeId` integer default null references `node` (`id`)\n" +
					");");
			statement.addBatch("create table `versions` (\n" +
					"\t`id` integer primary key autoincrement,\n" +
					"\t`name` text not null,\n" +
					"\t`files` text not null,\n" +
					"\t`paths` text not null,\n" +
					"\t`rootId` integer default null references `nodes` (`id`)\n" +
					");");
			statement.addBatch("alter table `nodes` add column `versionId` integer default null references `versions` (`id`);");
			statement.addBatch("create table `bases` (\n" +
					"\t`classId` integer not null references `node` (`id`),\n" +
					"\t`baseId` integer not null references `node` (`id`),\n" +
					"\tprimary key (`classId`, `baseId`)\n" +
					");");
			statement.addBatch("create table `parameters` (\n" +
					"\t`functionId` integer not null references `node` (`id`),\n" +
					"\t`parameterId` integer not null references `node` (`id`),\n" +
					"\tprimary key (`functionId`, `parameterId`)\n" +
					");");
			statement.addBatch("create table `uses` (\n" +
					"\t`versionId` integer not null references `versions` (`id`),\n" +
					"\t`nodeA` integer not null references `node` (`id`),\n" +
					"\t`nodeB` integer not null references `node` (`id`),\n" +
					"\t`typeEnum` integer not null,\n" +
					"\t`count` integer not null,\n" +
					"\tprimary key (`versionId`, `nodeA`, `nodeB`)\n" +
					");");
			statement.addBatch("create table `differences` (\n" +
					"\t`id` integer primary key autoincrement,\n" +
					"\t`versionA` integer not null references `versions` (`id`),\n" +
					"\t`versionB` integer not null references `versions` (`id`),\n" +
					"\t`nodeA` integer default null references `node` (`id`),\n" +
					"\t`nodeB` integer default null references `node` (`id`),\n" +
					"\t`typeEnum` integer not null,\n" +
					"\tunique (`versionA`, `versionB`, `nodeA`, `nodeB`)\n" +
					");");
			statement.executeBatch();
		}
	}
}
