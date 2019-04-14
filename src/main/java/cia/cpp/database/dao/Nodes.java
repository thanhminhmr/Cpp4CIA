package cia.cpp.database.dao;

import cia.cpp.database.bean.Node;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class Nodes {
	private Nodes() {
	}

	public static List<Node> list(Connection connection, Node value) throws SQLException {
		return Tables.select(connection, Node::new, value);
	}

	public static Node get(Connection connection, Node value) throws SQLException {
		final List<Node> list = Tables.select(connection, Node::new, value);
		return list.size() == 1 ? list.get(0) : null;
	}

	public static Node add(Connection connection, Node value) throws SQLException {
		return Tables.insert(connection, Node::new, value, "id");
	}

	public static Node update(Connection connection, Node whatValue, Node toValue) throws SQLException {
		return Tables.update(connection, Node::new, toValue, whatValue);
	}

	public static boolean delete(Connection connection, Node value) throws SQLException {
		return Tables.delete(connection, value) != 0;
	}
}