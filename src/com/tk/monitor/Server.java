package com.tk.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import com.tk.logger.Logging;
import com.tk.monitor.agent.Agent;
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
	private static final Logger log = Logging.getLogger("monitor");
	DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.MYSQL);

	private static final Server ins = new Server();

	private int offline;
	private boolean shutDown = true;
	private final String machineInsertDBStr = "insert into machinesinfo (id, gettime, online, totalmem, freemem, maxmem, totalphymem, "
			+ "freephymem, usedphymem, totalthread, cpuratio, totaldisk, useddisk,freedisk) "
			+ " values(%s,'%s',1,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s);";
	private final String interfaceInsertDBStr = "insert into interfaceinfo (collid, gettime, online, cost_time, callnum, lastcollid) "
			+ " values(%d,'%s',1,%f,%d,%d);";

	private List<Collection> colls = new LinkedList<Collection>();

	private Properties getProperties() {
		InputStream is = null;
		Properties pro = new Properties();
		if (is == null) {
			try {
				is = new FileInputStream(
						new File(Agent.class.getClassLoader().getResource("").toURI().getPath() + "/properties/server.properties"));
				pro.load(is);
			} catch (Exception e) {
				log.log(Level.SEVERE, "初始化出现异常,请检查配置文件.", e);
			}
		}
		return pro;
	}

	private Server() {
		Properties pro = getProperties();
		// 配置数据库
		try {
			DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.valueOf(pro.getProperty("DBType", "MYSQL")));
			dbh.init(pro.getProperty("DBServerAdd"), pro.getProperty("DBServerPort"), pro.getProperty("DBDataBaseName"),
					pro.getProperty("DBUserName"), pro.getProperty("DBPasswd"), Integer.parseInt(pro.getProperty("DBPoolNum")));

			offline = Integer.parseInt(pro.getProperty("offline", "10"));
		} catch (Exception e) {
			offline = 10;
			log.log(Level.SEVERE, "数据库配置出错,请检查." + e);
		}

		start();
	}

	/**
	 * 得到类的实例的方法.
	 * 
	 * @return 类的实例.
	 */
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
		log.finest("接受关闭线程指令.");
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
				sb.append("{type:" + coll.getType().ordinal() + ",id:" + coll.getId() + ",ispaush:"
						+ (coll.isPause() ? "true" : "false") + ",collinterval:" + coll.getInterval() + ",collid:"
						+ coll.getCollid() + ",lastCollTime:\"" + coll.getLastGetTime() + "\",collDimension:" + coll.getCollDimension()
						+ ",lastColl:" + coll.getLastColl()  + "},");
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);
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

		// 收到消息,激活一下对象.保存状态.
		for (Collection coll : colls) {
			if (coll.getId() == js.getInt("id")) {
				coll.collection();
			}
		}
		String[] sqls = new String[2];
		// 将数据写入持久层,并设置相应在线标志.
		switch (colltype) {
		case Machine:
			sqls[0] = String.format(machineInsertDBStr, js.get("id"), js.get("gettime"), js.get("totalmemory"),
					js.get("freememory"), js.get("maxmemory"), js.get("totalphysicalmemory"), js.get("freephysicalmemory"),
					js.get("usedphysicalmemory"), js.get("totalthread"), js.get("cpuratio"), js.get("totaldisk"),
					js.get("useddisk"), js.get("freedisk"));
			sqls[1] = "update machines set isonline = 1 where id = " + js.get("id") + " and isonline <> 1;";
			dbh.execBatchSql(sqls);
			break;
		case InterfaceCall:
			sqls[0] = String.format(interfaceInsertDBStr, js.get("id"), js.getString("gettime"), js.get("cost_time"),
					js.get("callnum"), js.get("lastcollid"));
			sqls[1] = "update machines set isonline = 1 where id = " + js.get("id") + " and isonline <> 1;";
			dbh.execBatchSql(sqls);
			break;
		default:
			break;
		}
	}

	/**
	 * 生成一个单独的线程启动本对象.
	 * 
	 * <p>
	 * 调用本方法,如本程序已经在运行中.则不会有任何反应.
	 */
	public void start() {
		if (!shutDown) {
			log.warning("Server对象正在执行中,不能再次开始.");
			return;
		}

		log.finer("Server对象初始化,并开始执行.");

		synchronized (this) {
			try {
				wait(300);
			} catch (InterruptedException e) {
			}
		}

		// 重新读取数据库的采集对象
		colls.clear();
		ResultSet rs = dbh.select("select * from machines;");
		try {
			while (rs.next()) {
				colls.add(getCollection(rs.getInt(FieldName.M_Flag), rs.getInt(FieldName.Id),
						rs.getInt(FieldName.M_CollInterval), rs.getInt(FieldName.M_CollDimension), rs.getInt(FieldName.M_IsPause),
						rs.getInt(FieldName.M_Agent), rs.getInt(FieldName.M_CollId)));
			}
		} catch (SQLException e) {
			log.log(Level.SEVERE, "", e);
		}

		dbh.close(rs);

		log.finest("程序启动.");

		// 启动线程
		Thread th = new Thread(this);
		shutDown = false;
		th.start();
	}

	private Collection getCollection(int type, int id, int collInterval, int collDimension, int ispause, int agent,
			int collid) {
		switch (CollectionType.values()[type]) {
		case Machine:
			return new Machines(this, id, agent, ispause == 0, collInterval * 1000, collid);
		case InterfaceCall:
			return new InterfaceColl(this, id, agent, collid, collInterval * 1000, collDimension, "", 0, ispause == 0);
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
			// 线程信息1秒
			try {
				wait(1000);
			} catch (InterruptedException e) {
				log.log(Level.SEVERE, "", e);
			}

			currentTime = System.currentTimeMillis();
			// 间隔时间到达,启动采集动作.
			for (Collection coll : colls) {
				if (currentTime > (coll.getLastRunTime() + coll.getInterval()) && !coll.isShutDown()) {
					coll.collection();
				}

				// 如果大于倍数,此值由配置文件配置.间隔时间到达还未收到消息.判断离线.
				if (currentTime > (coll.getLastRunTime() + offline * coll.getInterval())) {
					setOffline(coll);
				}
			}

		}

		log.info("执行完成,关闭线程中...");
		for (Collection coll : colls) {
			if (!coll.isShutDown()) {
				coll.stop();
			}
		}

		// 线程结束,通知其他等待的线程.
		notifyAll();
	}

	private void setOffline(Collection coll) {
		if (coll.isOnline()) {
			log.info("采集对象离线." + coll);
			dbh.update("update machines set isonline = 0 where id = " + coll.getId() + " and isonline = 1;");
			coll.setOnline(false);
		}
	}

}
