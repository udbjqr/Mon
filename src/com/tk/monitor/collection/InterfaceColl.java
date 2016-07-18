package com.tk.monitor.collection;

import java.sql.ResultSet;
import java.sql.SQLException;
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
	private static final DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.MYSQL);

	private final int id;
	private final int collobjId;
	private final CollectionType type = CollectionType.InterfaceCall;
	private final CollectionRecord collRecord;
	// 这个值就是以秒为单位
	private int collinterval;
	private final int collDimension;
	private String collconfinfo;
	private boolean pause = false;
	private final int agent;
	private long lastRunTime;
	private String lastGetTime;
	private long lastCollid;

	private Thread th;
	private boolean shutDown = true;
	private boolean online = true;

	private final String jsonStr;
	// 测试是否有数据可读的sql
	private final String testStr;
	// 读取数据的sql
	private final String readStr;

	public InterfaceColl(CollectionRecord collRecord, int id, int agent, int collobjId, int collinterval,
			int collDimension, String lastGetTime, int lastCollid, boolean pause) {
		this.id = id;
		this.collobjId = collobjId;
		this.collRecord = collRecord;
		this.agent = agent;
		this.collinterval = collinterval;
		this.pause = pause;
		this.collDimension = collDimension;
		this.lastCollid = lastCollid;

		// 当没有传入此时间数据时被调用,第一次服务端初始化将不传入此值。
		if (lastGetTime == null || lastGetTime.equals("")) {
			ResultSet rs = dbh.select("select ifnull(gettime,'1900-01-01') as lastruntime, ifnull(lastcollid,0) as lastcollid"
					+ " from interfaceinfo where collid = " + id + "; ");
			try {
				if (rs.next()) {
					this.lastGetTime = rs.getString("lastruntime");
					this.lastCollid = rs.getInt("lastcollid");
				} else {
					// if(lastGetTime == null || lastGetTime.equals("")){
					this.lastGetTime = "1900-01-01";
					this.lastCollid = 0;
				}

				dbh.close(rs);
			} catch (SQLException e) {
				log.log(Level.SEVERE, "读取数据出错.", e);
			}

		} else {
			this.lastGetTime = lastGetTime;
			this.lastCollid = lastCollid;
		}

		this.jsonStr = "{id:" + id + ",gettime:\"%1$s\"," + "cost_time:%2$f,callnum:%3$d,lastcollid:%4$d}";

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

		testStr = "select ifnull(min(log_date),'') as logdate from t_uigw_sysinvokelog  t where " + dimensionColName
				+ " = %1$d and t.log_date > date_add((select min(log_date) from t_uigw_sysinvokelog where log_date > '%2$s' and "
				+ dimensionColName + " = %1$d), interval %3$d  second);";
		// 读取数据的sql
		readStr = "select max(log_date) as gettime,ifnull(avg(cost_time),0) "
				+ "as cost_time,count(*) as callnum from t_uigw_sysinvokelog where log_date > '%s' and log_date < '%s'  and "
				+ dimensionColName + " = %d ;";
	}

	@Override
	public synchronized void run() {
		log.info("采集开始启动,id:" + collobjId);
		while (!shutDown) {
			// 停止相应间隔时间
			try {
				wait();
			} catch (InterruptedException e) {
				log.log(Level.SEVERE, "线程暂停失败:", e);
			}
			log.finest("开始采集数据.");

			// 读取数据的sql
			String oneLastDate = dbh.selectWithString(String.format(testStr, collobjId, lastGetTime, collinterval / 1000));
			String str;
			try {
				while (!oneLastDate.equals("")) { // 如果没有可采集的数据，返回""
					lastRunTime = System.currentTimeMillis();

					str = String.format(readStr, lastGetTime, oneLastDate, collobjId);
					ResultSet rs = dbh.select(str);

					while (rs.next()) {
						lastGetTime = rs.getString("gettime");

						str = String.format(jsonStr, lastGetTime, rs.getDouble("cost_time"), rs.getInt("callnum"), lastCollid);
					}
					dbh.close(rs);

					collRecord.save(type, str);
					log.finer("采集到数据:" + str);

					oneLastDate = dbh.selectWithString(String.format(testStr, collobjId, lastGetTime, collinterval / 1000));
				}
				log.finest("没有可采集的数据，暂停。");
			} catch (SQLException e1) {
				log.log(Level.SEVERE, "读取数据出错.", e1);
			}
		}

		log.finer("采集线程结束,id:" + collobjId);
	}

	@Override
	public synchronized void collection() {
		lastRunTime = System.currentTimeMillis();
		if (!shutDown) {
			notify();
		}
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
	public int getCollid() {
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

	@Override
	public boolean isOnline() {
		return online;
	}

	@Override
	public void setOnline(boolean online) {
		this.online = online;
	}

	@Override
	public String toString() {
		return "id:" + id + ".agentid:" + agent + ".collobjId:" + collobjId;
	}

	@Override
	public String getLastGetTime() {
		return lastGetTime;
	}
}
