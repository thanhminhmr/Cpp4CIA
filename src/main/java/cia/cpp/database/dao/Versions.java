package cia.cpp.database.dao;

import cia.cpp.database.bean.Version;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Versions {
	private Versions() {
	}

	public static List<Version> list(Connection connection, Version value) throws SQLException {
		return Tables.select(connection, Version::new, value);
	}

	public static Version get(Connection connection, Version value) throws SQLException {
		final List<Version> list = Tables.select(connection, Version::new, value);
		return list.size() == 1 ? list.get(0) : null;
	}

	public static Version add(Connection connection, Version value) throws SQLException {
		return Tables.insert(connection, Version::new, value, "id");
	}

	public static Version update(Connection connection, Version whatValue, Version toValue) throws SQLException {
		return Tables.update(connection, Version::new, toValue, whatValue);
	}

	public static boolean delete(Connection connection, Version value) throws SQLException {
		return Tables.delete(connection, value) != 0;
	}
}