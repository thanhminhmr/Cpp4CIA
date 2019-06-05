package mrmathami.cia.cpp.database.dao;

import mrmathami.cia.cpp.database.bean.Base;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Bases {
	private Bases() {
	}

	public static List<Base> list(Connection connection, Base value) throws SQLException {
		return Tables.select(connection, Base::new, value);
	}

	public static Base get(Connection connection, Base value) throws SQLException {
		final List<Base> list = Tables.select(connection, Base::new, value);
		return list.size() == 1 ? list.get(0) : null;
	}

	public static Base add(Connection connection, Base value) throws SQLException {
		return Tables.insert(connection, Base::new, value, null);
	}

	public static boolean delete(Connection connection, Base value) throws SQLException {
		return Tables.delete(connection, value) != 0;
	}
}