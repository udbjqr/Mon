package com.tk.monitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.tk.monitor.collection.Collection;
import com.tk.monitor.collection.CollectionRecord;
import com.tk.monitor.collection.CollectionType;
import com.tk.monitor.collection.Machines;
import com.tk.sql.DBType;
import com.tk.sql.DataBaseHandle;

/**
 * 服务对象.
 * 
 * @author yimin
 *
 */
public class Server implements CollectionRecord, Runnable{
	private static final Logger log = com.tk.monitor.logger.Logger.getLogger();
	DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.Mysql);

	private boolean shutDown = false;
	private final String machineInsertDBStr = "insert into machinesinfo (id, gettime, online, totalmem, freemem, maxmem, totalphymem, "
			+ "freephymem, usedphymem, totalthread, cpuratio, totaldisk, useddisk,freedisk) "
			+ " values(%s,'%s',1,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)";

	private List<Collection> colls = new LinkedList<Collection>();

	public boolean isShutDown() {
		return shutDown;
	}

	public synchronized void stop() {
		shutDown = true;
		this.notify();
	}

	@Override
	public void save(Collection coll, String collDataSql) {
		JSONObject js = new JSONObject(collDataSql);

		switch (coll.getType()) {
		case Machine:
			dbh.update(String.format(machineInsertDBStr, js.get("id"), js.get("gettime"), js.get("totalmemory"),
					js.get("freememory"), js.get("maxmemory"), js.get("totalphysicalmemory"), js.get("freephysicalmemory"),
					js.get("usedphysicalmemory"), js.get("totalthread"), js.get("cpuratio"), js.get("totaldisk"),
					js.get("useddisk"), js.get("freedisk")));
			break;
		default:
			break;
		}
	}

	/**
	 * 初始化动作.
	 */
	private void init() {
		log.finer("初始化开始.");
		
		colls.clear();
		ResultSet rs = dbh.select("select * from machines;");
		try {
			while (rs.next()) {
				colls.add(getCollection(rs.getInt(FieldName.M_Flag), rs.getInt(FieldName.Id),
						rs.getInt(FieldName.M_CollInterval), rs.getInt(FieldName.M_IsPause), rs.getInt(FieldName.M_Agent)));
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "", e);
		}

		dbh.close(rs);
		log.finer("初始化完成.");
	}

	private Collection getCollection(int type, int id, int collint, int ispause, int agent) {
		if (type == CollectionType.Machine.ordinal()) {
			return new Machines(this, agent, ispause == 0, collint, id);
		}
		return null;
	}

	/**
	 * 线程执行过程
	 */
	@Override
	public synchronized void run() {
		init();

		log.info("开始启动线程.");
		for (Collection coll : colls) {
			if (!coll.isPause() && coll.getAgent() == 0) {
				coll.start();
			}
		}

		while (!shutDown) {
			try {
				wait();
			} catch (InterruptedException e) {
				log.log(Level.SEVERE, "", e);
			}
		}

		log.info("执行完成,关闭线程中...");
		for (Collection coll : colls) {
			if (!coll.isShutDown()) {
				coll.stop();
			}
		}
	}

}
