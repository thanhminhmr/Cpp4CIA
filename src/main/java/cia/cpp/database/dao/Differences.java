package cia.cpp.database.dao;

import cia.cpp.database.bean.Difference;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Differences {
	private Differences() {
	}

	public static List<Difference> list(Connection connection, Difference value) throws SQLException {
		return Tables.select(connection, Difference::new, value);
	}

	public static Difference get(Connection connection, Difference value) throws SQLException {
		final List<Difference> list = Tables.select(connection, Difference::new, value);
			return list.size() == 1 ? list.get(0) : null;
	}

	public static Difference add(Connection connection, Difference value) throws SQLException {
		return Tables.insert(connection, Difference::new, value, "id");
	}

	public static Difference update(Connection connection, Difference whatValue, Difference toValue) throws SQLException {
		return Tables.update(connection, Difference::new, toValue, whatValue);
	}

	public static boolean delete(Connection connection, Difference value) throws SQLException {
		return Tables.delete(connection, value) != 0;
	}
}