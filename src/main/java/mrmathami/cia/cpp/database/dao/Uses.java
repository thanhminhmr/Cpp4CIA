package mrmathami.cia.cpp.database.dao;

import mrmathami.cia.cpp.database.bean.Use;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Uses {
	private Uses() {
	}

	public static List<Use> list(Connection connection, Use value) throws SQLException {
		return Tables.select(connection, Use::new, value);
	}

	public static Use get(Connection connection, Use value) throws SQLException {
		final List<Use> list = Tables.select(connection, Use::new, value);
		return list.size() == 1 ? list.get(0) : null;
	}

	public static Use add(Connection connection, Use value) throws SQLException {
		return Tables.insert(connection, Use::new, value, null);
	}

	public static Use update(Connection connection, Use whatValue, Use toValue) throws SQLException {
		return Tables.update(connection, Use::new, toValue, whatValue);
	}

	public static boolean delete(Connection connection, Use value) throws SQLException {
		return Tables.delete(connection, value) != 0;
	}
}