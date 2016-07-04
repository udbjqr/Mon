package com.tk.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * 实际的mysql操作对象.
 * 
 * <p>
 * 目前此对象每一次操作都新建一个连接对象进行操作
 * 
 * @author yimin
 *
 */
public final class MysqlHandle extends DataBaseHandle{
	private static final MysqlHandle ins = new MysqlHandle();
	private String connStr;
	private String user;
	private String passwd;

	public static MysqlHandle getIns() {
		return ins;
	}

	private MysqlHandle() {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.log(Level.SEVERE, "初始化mysqlJDBC出现异常:", e);
		}
	}

	@Override
	public void init(String server, String port, String db, String user, String passwd) {
		connStr = String.format("jdbc:mysql://%s:%s/%s", server, port, db);
		this.user = user;
		this.passwd = passwd;
	}

	@Override
	public int update(String sql) {
		Connection con = getConnection();
		int res = -1;
		try {
			Statement st = con.createStatement();
			res = st.executeUpdate(sql);
			
			log.finer("执行sql语句:" + sql + ",影响的行数:" + res);
		
			st.close();
			con.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "执行无返回sql语句发生异常,sql:" + sql, e);
		}
		
		return res;
	}

	@Override
	public int selectWithInt(String sql) {
		ResultSet set = select(sql);
		int res = -1;

		try {
			while (set.next()) {
				res = set.getInt(1);
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "获得ResultSet数据异常:", e);
		}

		close(set);
		return res;
	}

	@Override
	public ResultSet select(String sql) {
		Connection con = getConnection();
		try {
			Statement st = con.createStatement();
			ResultSet set = st.executeQuery(sql);
			log.finer("执行sql语句:" + sql);
			
			return set;
		} catch (SQLException e) {
			log.log(Level.SEVERE, "执行sql语句发生异常,sql:" + sql, e);
		}
		return null;
	}

	@Override
	public void close(ResultSet set) {
		try {
			Statement st = set.getStatement();
			Connection con;
			con = st.getConnection();
			set.close();
			st.close();
			con.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "关闭JDBC相应对象出现异常:", e);
		}
	}

	@Override
	protected Connection getConnection() {
		try {
			return DriverManager.getConnection(connStr, user, passwd);
		} catch (SQLException e) {
			log.log(Level.SEVERE, "数据连接异常:", e);
			return null;
		}
	}

	@Override
	public void execBatchSql(String[] sqls) {
		Connection con = getConnection();
		StringBuilder sb = new StringBuilder();
		
		try {
			con.setAutoCommit(false);
			
			Statement st = con.createStatement();

			for (String sql : sqls) {
				st.addBatch(sql);
				sb.append(sql + "\n");
			}
			
			st.executeBatch();
			
			log.finer("执行批量sql语句:" + sb.toString());
			
			con.commit();
			con.close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "执行sql语句发生异常.sqls:" + sb.toString(), e);
			try {
				con.rollback();
				con.close();
			} catch (SQLException e1) {
				log.log(Level.SEVERE, "执行关闭发生异常.", e);
			}
		}
	}
}