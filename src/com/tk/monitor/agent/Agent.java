package com.tk.monitor.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
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
 * 此类为采集的代理类.
 * 
 * @author yimin
 *
 */
public class Agent implements CollectionRecord, Runnable{
	private static final Logger log = Logging.getLogger("agent");
	private String sessionStr = null;
	private final String serverAddr;
	private URL url = null;

	private Thread th;

	private boolean shutDown = true;

	private final List<Collection> colls = new LinkedList<Collection>();

	private final int id;

	public synchronized void Init() {
		shutDown = true;
		colls.clear();
		// 读取相应数据,
	}

	/**
	 * 返回服务器端的IP地址.
	 * 
	 * @return
	 */
	public String getServerAddr() {
		return serverAddr;
	}

	// public synchronized void add(Collection coll) {
	// colls.add(coll);
	// this.notify();
	// }

	// public synchronized void remove(Collection coll) {
	// coll.stop();
	// colls.remove(coll);
	// this.notify();
	// }

	/**
	 * 启动此agent线程.
	 */
	public synchronized void start() {
		if (shutDown == true) {
			shutDown = false;

			th = new Thread(this);
			th.start();

			notify();
		} else {
			log.warning("线程已经启动,不能再次启动.");
		}
	}

	/**
	 * 关闭正在执行的线程,如未启动,调用无效果.
	 */
	public synchronized void stop() {
		if (shutDown) {
			log.warning("线程未启动,不需要关闭.");
			return;
		}
		log.finer("Agent接受关闭指令.");
		shutDown = true;
		notify();
	}

	/**
	 * 代理启动过程.
	 */
	@Override
	public void run() {
		log.fine("启动Agent线程.");

		long currentTime = 0;

		synchronized (this) {
			// 启动所有采集数据
			for (Collection coll : colls) {
				if (!coll.isPause() && coll.isShutDown()) {
					coll.start();
				}
			}

			while (!shutDown) {
				try {
					wait(1000);
				} catch (InterruptedException e) {
					log.log(Level.SEVERE, "", e);
				}

				// 判断是否到达启动时间.每1秒启动检查一次,因此采集最小时间间隔就是1秒.
				currentTime = System.currentTimeMillis();
				for (Collection coll : colls) {
					if (currentTime > (coll.getLastRunTime() + coll.getInterval())) {
						coll.collection();
					}
				}
			}

			// 结束的处理
			for (Collection coll : colls) {
				log.fine("开始Agent的结束过程.");
				if (!coll.isShutDown()) {
					coll.stop();
				}
			}

			log.finest("Agent线程执行结束.");
		}

	}

	private Properties getProperties() {
		InputStream is = null;
		Properties pro = new Properties();
		if (is == null) {
			try {
				is = new FileInputStream(
						new File(Agent.class.getClassLoader().getResource("").toURI().getPath() + "/properties/Agent.properties"));
				pro.load(is);
			} catch (Exception e) {
				log.log(Level.SEVERE, "初始化出现异常,请检查配置文件.", e);
			}
		}
		return pro;
	}

	/**
	 * 构造函数,在这函数里构造所有采集对象.
	 * 
	 * @param serverAddr
	 *          服务端IP地址.
	 */
	public Agent() {
		Properties pro = getProperties();
		// 读配置文件,配置相关信息.
		if (pro.containsKey("ServerIPAdd")) {
			this.serverAddr = pro.getProperty("ServerIPAdd");
			try {
				url = new URL("http://" + serverAddr + "/monitor/Agent");
			} catch (MalformedURLException e) {
				log.log(Level.SEVERE, "服务器IP地址无法解析,请检查配置文件ServerIPAdd字段!", e);
			}
			// 配置数据库
			if (pro.getProperty("NeedDBSupport", "false").equals("true")) {
				try {
					DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.valueOf(pro.getProperty("DBType", "MYSQL")));
					dbh.init(pro.getProperty("DBServerAdd"), pro.getProperty("DBServerPort"), pro.getProperty("DBDataBaseName"),
							pro.getProperty("DBUserName"), pro.getProperty("DBPasswd"),
							Integer.parseInt(pro.getProperty("DBPoolNum")));
				} catch (Exception e) {
					log.log(Level.SEVERE, "数据库配置出错,请检查." + e);
				}
			}
		} else {
			this.serverAddr = "";
			log.severe("未找到ServerIPAdd字段,请检查配置文件ServerIPAdd字段");
		}

		// 向服务端发送服务端配置信息
		log.fine("准备向 " + url + " 发送数据");
		JSONObject jo = new JSONObject(exchangData("init=0"));

		id = jo.getInt("agent");

		// "{res:true,agent:xx,data:[{type:xx,ispaush:xx,collinterval:xx,collid:xx,
		// lastCollTime:"XX",collDimension:xx,lastColl:"XX"}...]}"
		for (Object obj : jo.getJSONArray("data")) {
			JSONObject j = (JSONObject) obj;
			switch (CollectionType.values()[j.getInt("type")]) {
			case Machine:
				colls.add(new Machines(this, j.getInt("id"), id, j.getBoolean("ispaush"), j.getInt("collinterval"),
						j.getInt("collid")));
				break;
			case InterfaceCall:
				colls.add(new InterfaceColl(this, j.getInt("id"), id, j.getInt("collid"), j.getInt("collinterval"),
						j.getInt("collDimension"), j.getLong("lastCollTime"), j.getInt("lastColl"), j.getBoolean("ispaush")));
				break;
			default:
				log.severe("错误的类型.值:" + j.getInt("type"));
				break;
			}
		}
	}

	/**
	 * 保存数据.
	 * <p>
	 * agnet的保存将数据向远端服务器发送数据.并接收确认保存消息.
	 */
	@Override
	public synchronized void save(CollectionType collType, String collDataJson) {
		exchangData("type=" + collType.ordinal() + "&data=" + collDataJson);
	}

	/**
	 * 使用http协议将数据发送至远端.
	 * 
	 * @param data
	 *          要传送的数据
	 * @return 收到远端响应的数据.
	 */
	private String exchangData(String data) {
		log.fine("准备发送数据:" + data);

		BufferedReader br = null;
		HttpURLConnection conn = null;
		StringBuilder sb = null;

		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("POST");
			if (sessionStr != null) {
				conn.setRequestProperty("Cookie", sessionStr);
			}
			conn.setUseCaches(false); // Post can not user cache
			conn.setDoOutput(true); // set output from urlconn
			conn.setDoInput(true); // set input from urlconn

			OutputStream out = conn.getOutputStream();
			out.write(data.getBytes("utf-8"));
			out.flush();
			out.close();

			if (sessionStr != null) {
				String session_value = conn.getHeaderField("Set-Cookie");
				String[] sessionId = session_value.split(";");
				sessionStr = "JSESSIONID=" + sessionId[0];
			}

			br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			sb = new StringBuilder();
			String brLine;
			while ((brLine = br.readLine()) != null) {
				sb.append(brLine);
			}

			log.fine("发送数据返回:" + sb.toString());

			if (br != null) {
				br.close();
			}

		} catch (Exception e) {
			if (e instanceof ConnectException) {
				log.severe("与服务器连接断开,关闭代理.");
				stop();
			}
			log.log(Level.SEVERE, "", e);
		} finally {
			conn.disconnect();
		}

		if (sb != null) {
			return sb.toString();
		}
		return null;
	}

	public boolean isShutDown() {
		return shutDown;
	}
}
