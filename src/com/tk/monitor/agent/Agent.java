package com.tk.monitor.agent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.tk.monitor.collection.Collection;
import com.tk.monitor.collection.CollectionRecord;
import com.tk.monitor.collection.CollectionType;
import com.tk.monitor.collection.Machines;

/**
 * 此类为采集的代理类.
 * 
 * @author yimin
 *
 */
public class Agent implements CollectionRecord, Runnable{
	private static final Logger log = com.tk.logger.Logger.getLogger();
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

	public String getServerAddr() {
		return serverAddr;
	}

	public synchronized void add(Collection coll) {
		colls.add(coll);
		this.notify();
	}

	public synchronized void remove(Collection coll) {
		coll.stop();
		colls.remove(coll);
		this.notify();
	}

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

	public synchronized void stop() {
		log.finer("Agent接受关闭指令.");
		shutDown = true;
		notify();
	}

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
				currentTime = System.currentTimeMillis();
				for (Collection coll : colls) {
					if (currentTime > (coll.getLastRunTime() + coll.getInterval())) {
						coll.collection();
					}
				}

				try {
					wait(1000);
				} catch (InterruptedException e) {
					log.log(Level.SEVERE, "", e);
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

	public Agent(String serverAddr) {
		this.serverAddr = serverAddr;

		try {
			url = new URL("http://" + serverAddr + "/monitor/Agent");
		} catch (MalformedURLException e) {
			log.log(Level.SEVERE, "", e);
		}

		log.fine("准备向 " + url + " 发送数据");
		JSONObject jo = new JSONObject(exchangData("init=0"));

		id = jo.getInt("agent");

		// "{res:true,agent:1,data:[{type:0,ispaush:1,collinterval:2,collid:2}]}"
		for (Object obj : jo.getJSONArray("data")) {
			JSONObject j = (JSONObject) obj;
			switch (CollectionType.values()[j.getInt("type")]) {
			case Machine:
				colls.add(new Machines(this, id, j.getBoolean("ispaush"), j.getInt("collinterval"), j.getInt("collid")));
				break;
			default:
				log.severe("错误的类型.");
				break;
			}
		}
	}

	@Override
	public void save(CollectionType collType, String collDataJson) {
		exchangData("type=" + collType.ordinal() + "&data=" + collDataJson);
	}

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
				log.info("服务器连接断开,关闭代理.");
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
