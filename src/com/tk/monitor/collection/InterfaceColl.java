package com.tk.monitor.collection;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.tk.logger.Logging;
import com.tk.sql.DBType;
import com.tk.sql.DataBaseHandle;

/**
 * 实际的接口调用采集对象.
 * 
 * <p>
 * 此对象每一个针对一个采集方式,需要采集多个对象时,设置多个采集.
 * <p>
 * 此对象使用collconfinfo传送lastcollid值.
 * 
 * @author yimin
 *
 */
public class InterfaceColl implements Collection{
	private static final Logger log = Logging.getLogger("Collection");
	private static final DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.Mysql);

	private final int id;
	private final int collobjId;
	private final CollectionType type = CollectionType.InterfaceCall;
	private final CollectionRecord collRecord;
	private int collinterval;
	private final int collDimension;
	private String collconfinfo;
	private boolean pause = false;
	private final int agent;
	private long lastRunTime;
	private long lastCollid;

	private final Date dat = new Date();
	private Thread th;
	private boolean shutDown = true;

	private final String jsonStr;
	// 测试是否有数据可读的sql
	private final String testStr;
	// 读取数据的sql
	private final String readStr;

	public InterfaceColl(CollectionRecord collRecord, int id,int agent, int collobjId, int collinterval, int collDimension,
			long lastRunTime, int lastCollid, boolean pause) {
		init();
		this.id = id;
		this.collobjId = collobjId;
		this.collRecord = collRecord;
		this.agent = agent;
		this.collinterval = collinterval;
		this.pause = pause;
		this.collDimension = collDimension;
		this.lastCollid = lastCollid;

		// 调用者是服务端,从数据库取相应数据.
		if (agent == 0) {
			ResultSet rs = dbh.select(
					"select ifnull(max(gettime),'01/01/1900 01:01:01') as lastruntime, ifnull(max(lastcollid),0) as lastcollid"
							+ " from interfaceinfo where id = " + collobjId + "; ");
			try {
				while (rs.next()) {
					this.lastRunTime = rs.getDate("lastruntime").getTime();
					this.lastCollid = rs.getInt("lastcollid");
				}
			} catch (SQLException e) {
				log.log(Level.SEVERE, "读取数据出错.", e);
			}

		} else {
			this.lastRunTime = lastRunTime;
			this.lastCollid = lastCollid;
		}

		this.jsonStr = "{id:" + id + ",gettime:\"%1$tY-%1$tm-%1$td %1$tT.%1$tL\","
				+ "cost_time:%2$d,callnum:%3$d,lastcollid:%4$d}";

		String dimensionColName = "interface_id";

		switch (collDimension) {
		case 1: // 这个是读取接口interface_id的数据
			break;
		case 2: // 读取system_id 的数据
			dimensionColName = "system_id";
			break;
		default:
			log.severe("错误的粒度指明.数据应该在1..2之间");
			break;
		}

		testStr = "select ifnull(min(id),-1) as id  from t_uigw_sysinvokelog  t where  id > %1$d" + " and "
				+ dimensionColName + " = %2$d and t.log_date > (log_date,interval %3$d minute));";
		// 读取数据的sql
		readStr = "select ifnull(max(collobjId),0) as lastid,max(log_date) as gettime,ifnull(avg(cost_time),0) as cost_time,count(*) as callnum "
				+ "from t_uigw_sysinvokelog where id > %1$d and id <= %2$d  and " + dimensionColName + " = %3$d ;";
	}

	private void init() {
		dbh.init("127.0.0.1", "3306", "test", "root", "123456", 2);
		// TODO 此方法初始化对连接对象
	}

	@Override
	public void run() {
		log.info("采集开始启动,id:" + collobjId);
		while (!shutDown) {
			log.finest("开始采集数据.");

			String str = String.format(testStr, lastCollid, collobjId, collinterval);
			for (int minid = dbh.selectWithInt(str); minid > 0; minid = dbh.selectWithInt(str)) {
				lastRunTime = System.currentTimeMillis();

				try {
					str = String.format(readStr, lastCollid, minid, collobjId);
					ResultSet rs = dbh.select(str);

					while (rs.next()) {
						lastCollid = rs.getLong("lastid");

						dat.setTime(lastRunTime);
						str = String.format(jsonStr, rs.getString("gettime"), rs.getDouble("cost_time"), rs.getInt("callnum"), rs.getLong("lastid"));
					}

					dbh.close(rs);

					collRecord.save(type, str);
					log.finer("采集到数据:" + str);
				} catch (SQLException e1) {
					log.log(Level.SEVERE, "读取数据出错.", e1);
				}
			}
			// 停止相应间隔时间
			try {
				wait();
			} catch (InterruptedException e) {
				log.log(Level.SEVERE, "线程暂停失败:", e);
			}
		}

		log.finer("采集线程结束,id:" + collobjId);
	}

	@Override
	public synchronized void collection() {
		notify();
	}

	@Override
	public void start() {
		if (shutDown) {
			shutDown = false;
			th = new Thread(this);
			th.start();
		}
	}

	@Override
	public synchronized void stop() {
		if (!shutDown) {
			shutDown = true;
			notify();
		}
	}

	@Override
	public boolean isShutDown() {
		return shutDown;
	}

	@Override
	public boolean isPause() {
		return pause;
	}

	@Override
	public void setPause(boolean pause) {
		this.pause = pause;
	}

	@Override
	public int getAgent() {
		return agent;
	}

	@Override
	public CollectionType getType() {
		return type;
	}

	@Override
	public int getInterval() {
		return collinterval;
	}

	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public int getCollid(){
		return collobjId;
	}

	@Override
	public long getLastRunTime() {
		return lastRunTime;
	}

	@Override
	public String getLastColl() {
		return String.valueOf(lastCollid);
	}

	@Override
	public int getCollDimension() {
		return collDimension;
	}

	public String getCollconfinfo() {
		return collconfinfo;
	}

}
