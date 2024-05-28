package cc.baka9.catseedlogin.bukkit.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class BufferStatement {
	private Object[] values;
	private String query;
	private Exception stacktrace;

	/**
	 * Represents a PreparedStatement in a state before preparing it (E.g. No
	 * file I/O Required)
	 * 
	 * @param query
	 *            The query to execute. E.g. INSERT INTO accounts (user, passwd)
	 *            VALUES (?, ?)
	 * @param values
	 *            The values to replace <bold>?</bold> with in
	 *            <bold>query</bold>. These are in order.
	 */
	private static final Exception sharedException = new Exception();

	public BufferStatement(String query, Object... values) {
		this.query = query;
		this.values = values;
		this.stacktrace = sharedException; // 重复利用一个已存在的 Exception 对象
		this.stacktrace.fillInStackTrace();
	}

	/**
	 * Returns a prepared statement using the given connection. Will try to
	 * return an empty statement if something went wrong. If that fails, returns
	 * null.
	 * 
	 * This method escapes everything automatically.
	 * 
	 * @param con
	 *            The connection to prepare this on using
	 *            con.prepareStatement(..)
	 * @return The prepared statement, ready for execution.
	 */
	public PreparedStatement prepareStatement(Connection con) throws SQLException {
		PreparedStatement ps;
		int valuesLength = values.length; // 缓存数组长度
		ps = con.prepareStatement(query);
		for (int i = 0; i < valuesLength; i++) {
			ps.setObject(i + 1, values[i]);
		}
		return ps;
	}

	/**
	 * Used for debugging. This stacktrace is recorded when the statement is
	 * created, so printing it to the screen will provide useful debugging
	 * information about where the query came from, if something went wrong
	 * while executing it.
	 * 
	 * @return The stacktrace elements.
	 */
	public StackTraceElement[] getStackTrace() {
		return stacktrace.getStackTrace();
	}

	/**
	 * @return A string representation of this statement. Returns
	 *         <italic>"Query: " + query + ", values: " +
	 *         Arrays.toString(values).</italic>
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Query: ").append(query).append(", values: ").append(Arrays.toString(values));
		return sb.toString();
	}
}