package com.tk.monitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.tk.logger.Logging;
import com.tk.monitor.collection.Collection;
import com.tk.monitor.collection.CollectionRecord;
import com.tk.monitor.collection.CollectionType;
import com.tk.monitor.collection.InterfaceColl;
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
	private static final Logger log = Logging.getLogger();
	DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.Mysql);

	private static final Server ins = new Server();

	private boolean shutDown = false;
	private final String machineInsertDBStr = "insert into machinesinfo (id, gettime, online, totalmem, freemem, maxmem, totalphymem, "
			+ "freephymem, usedphymem, totalthread, cpuratio, totaldisk, useddisk,freedisk) "
			+ " values(%s,'%s',1,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)";
	private final String interfaceInsertDBStr = "insert into interfaceinfo (collid, gettime, online, cost_time, callnum, lastcollid) "
			+ " values(%s,%s,1,%s,%s,%s);";

	private List<Collection> colls = new LinkedList<Collection>();

	private Server() {
		init();
	}

	public static Server getIns() {
		return ins;
	}

	/**
	 * 此服务是否已经停止.
	 * 
	 * <p>
	 * 当stop()时,设置此值,必须等线程运行完全自动停止,而不是立即终止
	 * 
	 * @return false 正在运行.
	 */
	public boolean isShutDown() {
		return shutDown;
	}

	/**
	 * 停止服务.
	 */
	public synchronized void stop() {
		shutDown = true;
		this.notify();
	}

	/**
	 * 返回代理请求的所有相关信息.
	 * 
	 * <p>
	 * 包含代理所需要采集相关信息.
	 * 
	 * @param ipAdd
	 *          代理的IP.根据此值确定某一个代理
	 * @return 一个json格式的字符串,指明代理需要的信息.
	 */
	public String getAgentCollectionsJson(String ipAdd) {
		StringBuilder sb = new StringBuilder();
		int agentId = dbh.selectWithInt("select * from agent where ipadd = '" + ipAdd + "';");
		// 没有找到对应IP的记录.
		if (agentId == Integer.MIN_VALUE) {
			log.warning("未找到对应ip的记录,请检查,IP:" + ipAdd);
		}

		// "{res:true,agent:xx,data:[{type:xx,ispaush:xx,collinterval:xx,collid:xx,
		// lastCollTime:"XX",collDimension:xx,lastColl:"XX"}...]}"
		for (Collection coll : colls) {
			if (coll.getAgent() == agentId) {
				sb.append(
						"{type:" + coll.getType().ordinal() + ",id:" + coll.getId() + ",ispaush:" + (coll.isPause() ? "true" : "false") + ",collinterval:"
								+ coll.getInterval() / 1000 + ",collid:" + coll.getCollid() + ",lastCollTime:" + coll.getLastRunTime()
								+ ",collDimension:" + coll.getCollDimension() + ",lastColl:" + coll.getLastColl() + "},");
			}
		}
		if (sb.length() > 0) {
			sb.delete(sb.length() - 1, sb.length());
		}
		sb.insert(0, "{res:true,agent:" + agentId + ",data:[");
		sb.append("]}");

		log.fine("准备向agent传送数据:" + sb.toString());
		return sb.toString();
	}

	/**
	 * 将采集到的数据保存到持久化的过程.
	 */
	@Override
	public void save(CollectionType colltype, String collDataSql) {
		JSONObject js = new JSONObject(collDataSql);

		switch (colltype) {
		case Machine:
			dbh.update(String.format(machineInsertDBStr, js.get("id"), js.get("gettime"), js.get("totalmemory"),
					js.get("freememory"), js.get("maxmemory"), js.get("totalphysicalmemory"), js.get("freephysicalmemory"),
					js.get("usedphysicalmemory"), js.get("totalthread"), js.get("cpuratio"), js.get("totaldisk"),
					js.get("useddisk"), js.get("freedisk")));
			break;
		case InterfaceCall:
			dbh.update(String.format(interfaceInsertDBStr, js.getString("id"),js.getString("gettime"),
					js.getString("cost_time"),js.getString("callnum"),js.getString("lastcollid")));
			break;
		default:
			break;
		}
	}

	/**
	 * 初始化动作.
	 */
	public void init() {
		log.finer("初始化开始.");

		if (!shutDown) {
			stop();

			synchronized (this) {
				try {
					wait(500);
				} catch (InterruptedException e) {
				}
			}
		}

		colls.clear();
		ResultSet rs = dbh.select("select * from machines;");
		try {
			while (rs.next()) {
				colls.add(
						getCollection(rs.getInt(FieldName.M_Flag),rs.getInt(FieldName.Id),rs.getInt(FieldName.M_CollId), rs.getInt(FieldName.M_CollInterval),
								rs.getInt(FieldName.M_CollDimension), rs.getInt(FieldName.M_IsPause), rs.getInt(FieldName.M_Agent)));
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "", e);
		}

		dbh.close(rs);
		log.finer("初始化完成.");
	}

	private Collection getCollection(int type, int id, int collInterval, int collDimension, int ispause, int agent,int collid) {
		switch (CollectionType.values()[type]) {
		case Machine:
			return new Machines(this,id, agent, ispause == 0, collInterval, collid);
		case InterfaceCall:
			return new InterfaceColl(this,id, agent, collid, collInterval, collDimension, 0, 0, ispause == 0);
		default:
			break;
		}
		if (type == CollectionType.Machine.ordinal()) {

		}
		return null;
	}

	/**
	 * 线程执行过程.
	 */
	@Override
	public synchronized void run() {
		log.info("开始启动线程.");

		for (Collection coll : colls) {
			if (!coll.isPause() && coll.getAgent() == 0 && coll.isShutDown()) {
				coll.start();
			}
		}

		long currentTime = 0;
		while (!shutDown) {
			currentTime = System.currentTimeMillis();
			for (Collection coll : colls) {
				if (currentTime > (coll.getLastRunTime() + coll.getInterval())) {
					coll.collection();
				}
			}

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
