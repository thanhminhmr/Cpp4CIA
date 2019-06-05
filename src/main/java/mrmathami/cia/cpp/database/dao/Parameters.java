package mrmathami.cia.cpp.database.dao;

import mrmathami.cia.cpp.database.bean.Parameter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Parameters {
	private Parameters() {
	}

	public static List<Parameter> list(Connection connection, Parameter value) throws SQLException {
		return Tables.select(connection, Parameter::new, value);
	}

	public static Parameter get(Connection connection, Parameter value) throws SQLException {
		final List<Parameter> list = Tables.select(connection, Parameter::new, value);
		return list.size() == 1 ? list.get(0) : null;
	}

	public static Parameter add(Connection connection, Parameter value) throws SQLException {
		return Tables.insert(connection, Parameter::new, value, null);
	}

	public static boolean delete(Connection connection, Parameter value) throws SQLException {
		return Tables.delete(connection, value) != 0;
	}
}