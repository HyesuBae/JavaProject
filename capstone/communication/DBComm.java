package communication;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DBComm {
	private static final String ELECTIONS = "electionS";
	private static final String DB_ID = "root";
	private static final String DB_PW = "voting";

	public Connection dbConnect(String _dbName) {
		String dbURL = "jdbc:mysql://localhost:3306/" + _dbName
				+ "?useUnicode=true&characterEncoding=utf8";

		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection(dbURL, DB_ID, DB_PW);
			return conn;

		} catch (ClassNotFoundException e) {
			System.out.println(_dbName + " JDBC Connection Failed!!!");
		} catch (SQLException e) {
			System.out.println(_dbName + " JDBC Connection Failed!!!");
		}

		return null;
	}

	public boolean isTableUsed(String _tableName, Connection _conn) {
		String query = "show tables like '" + _tableName + "'";
		ResultSet rs = stmtExecuteQuery(query, _conn);
		try {
			if (rs.next()) {
				return true; // table exists
			}
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public String createTableName(String _electionId, String _type) {
		return _electionId + "_" + _type;
	}

	public boolean dropTable(String _dbName, String _tableName) {
		Connection conn = dbConnect(_dbName);
		if (conn == null)
			return false;

		if (!isTableUsed(_tableName, conn))
			return true;

		StringBuilder sb = new StringBuilder("drop table ");
		sb.append(_tableName);

		String query = sb.toString();

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(query);
		} catch (SQLException e) {
			System.out.println("drop table err");
			e.printStackTrace();
		}
		return true;

	}

	public boolean deleteElection(String _electionId) {
		Connection conn = dbConnect(ELECTIONS);
		if (conn == null)
			return false;

		String query = "delete from elections where electionId ='"
				+ _electionId + "'";

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(query);

		} catch (SQLException e) {
			System.out.println("delete from election err");
			e.printStackTrace();
		}

		return true;
	}

	public boolean stmtExecute(String _query, Connection _conn) {
		try {
			Statement stmt = _conn.createStatement();
			stmt.execute(_query);
		} catch (SQLException e) {
			System.out.println("stmt Execute err");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public ResultSet stmtExecuteQuery(String _query, Connection _conn) {
		try {
			Statement stmt = _conn.createStatement();
			ResultSet rs = stmt.executeQuery(_query);

			return rs;
		} catch (SQLException e) {
			System.out.println("stmt execute query err");
			e.printStackTrace();
			return null;
		}
	}

	public String selectQuery(String _tableName, ArrayList<String> _columns) {
		StringBuilder sb = new StringBuilder("select ");
		String prefix = "";
		for (int i = 0; i < _columns.size(); i++) {
			sb.append(prefix);
			prefix = ",";
			sb.append(_columns.get(i));
		}
		sb.append(" from ");
		sb.append(_tableName);

		return sb.toString();
	}

	public String decryptSelectQuery(String _tableName, String _key,
			ArrayList<String> _columns) {
		StringBuilder sb = new StringBuilder("select ");
		String prefix = "";
		for (int i = 0; i < _columns.size(); i++) {
			sb.append(prefix);
			prefix = ",";
			sb.append("AES_DECRYPT(");
			sb.append(_columns.get(i));
			sb.append(",'");
			sb.append(_key);
			sb.append("')");
		}
		sb.append(" from ");
		sb.append(_tableName);

		return sb.toString();
	}

	public boolean deleteData(String _tableName, String _column, String _value,
			String _dbName) {

		Connection conn = dbConnect(_dbName);
		if(conn == null)
			return false;
		
		String query = "delete from " + _tableName + " where " + _column + "='"
				+ _value + "'";

		System.out.println(query);
		return stmtExecute(query, conn);
	}

}
