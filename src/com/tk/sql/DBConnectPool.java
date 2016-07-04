package com.tk.sql;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Connection;

/**
 * 数据库连接池对象.
 * <p>
 * 此对象为单一对象
 * <p>
 * 此对象支持JDBC的连接池,同时保持一个ResultSet和本对象的映射,以便于在关闭resultSet的时候能找到相对应的connect.
 * 
 * @author yimin
 *
 */
public class DBConnectPool{
	private static final Logger log = com.tk.monitor.logger.Logger.getLogger();
	private static final DBConnectPool ins = new DBConnectPool();
	private final Map<Connection, com.tk.sql.Connection> rc = new Hashtable<>();

	private final LinkedList<com.tk.sql.Connection> cons = new LinkedList<com.tk.sql.Connection>();
	private com.tk.sql.Connection nowFree = null;
	private int size = 0;

	public static final DBConnectPool getIns() {
		return ins;
	}

	private DBConnectPool() {
	}

	public synchronized Connection getFreeConn() {
		com.tk.sql.Connection now = nowFree;
		// 找到一个空闲的连接.
		for (int i = 1; now.isBusy(); i++) {
			if (i % size == 0) {
				// 如果所有都忙,等待.
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}

			nowFree = nowFree.next;
		}

		nowFree = now.next;
		now.setbusy(true);
		return now;
	}

	public synchronized void add(Connection con, com.tk.sql.Connection myconn) {
		rc.put(con, myconn);

		size++;

		if (cons.isEmpty()) {
			cons.add(myconn);
			nowFree = myconn;
		} else {
			cons.getLast().next = myconn;
			myconn.next = cons.getFirst();
			cons.offer(myconn);
		}
		
		notify();
	}

	public synchronized void remove(com.tk.sql.Connection con) {
		for (com.tk.sql.Connection conn : cons) {
			if (conn.next == con) {
				conn.next = con.next;
				cons.remove(con);
			}
		}

		// 有可能出现问题.在遍历里面删除数据.
		for (Map.Entry<Connection, com.tk.sql.Connection> rcs : rc.entrySet()) {
			if (rcs.getValue() == con) {
				rc.remove(rcs.getKey());
			}
		}

		this.size--;
	}

	public void closeConnection(Connection con) {
		try {
			rc.get(con).close();
		} catch (SQLException e) {
			log.log(Level.SEVERE, "关闭连接出现异常", e);
		}
	}
}
