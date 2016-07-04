package com.tk.sql;

import java.sql.ResultSet;
import java.util.logging.Logger;
import java.sql.Connection;

/**
 * 数据库操作层面接口.此对象负责操作数据库
 * @author yimin
 *
 */
public abstract class DataBaseHandle{
	protected static final Logger log = com.tk.monitor.logger.Logger.getLogger();
	
	public static DataBaseHandle getDBHandle(DBType type){
		switch (type) {
		case Mysql:
			return MysqlHandle.getIns();
			//后面所有均直接返回空.
		case mssqlserver:
		case Oracle:
		case Pgsql:
		default:
			log.severe("错误的调用数据库初始化对象.");
			return null;
		}
	}
	
	/**
	 * 返回一个连接对象.
	 * @return 针对当前数据库的连接对象
	 */
	protected abstract Connection getConnection();
	
	public abstract void init(String server,String port,String db,String user,String passwd);
	
	/**
	 * 执行一个更新或者删除操作.
	 * @param str 要执行的sql语句.
	 * @return 成功影响的条数
	 */
	public abstract int update(String str);

	/**
	 * 执行一个只返回单个值的select语句,此操作不需要关闭动作.
	 * @param str 需要执行的sql语句
	 * @return 执行结果的一个int值.
	 */
	public abstract int selectWithInt(String str);
	
	/**
	 * 执行一个查询过程,使用完毕必须使用close()来关闭.
	 * @param str 需要执行的查询语句.
	 * @return 一个结果集.用完此结果集需要调用close()过程进行关闭操作.
	 */
	public abstract ResultSet select(String str);
	
	/**
	 * 关闭一个结果集对应的所有对象.
	 * @param set 本对象返回的结果集.
	 */
	public abstract void close(ResultSet set);
	
	/**
	 * 执行一系列sql语句.
	 * @param sqls
	 */
	public abstract void execBatchSql(String[] sqls);
}

