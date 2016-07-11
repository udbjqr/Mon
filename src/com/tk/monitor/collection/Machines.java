package com.tk.monitor.collection;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.management.OperatingSystemMXBean;
import com.tk.logger.Logging;

import sun.management.ManagementFactoryHelper;

/**
 * 采集机器信息的对象.
 * 
 * @author yimin
 *
 */
public class Machines implements Collection{
	private static final Logger log = Logging.getLogger("Collection");

	private int collInterval;
	private final int id;
	private final int machineId;
	private final CollectionRecord collRecord;
	private String jsonStr;
	private final CollectionType type = CollectionType.Machine;

	private Thread th;

	private long totalMemory = 0;
	private long freeMemory = 0;
	private long maxMemory = 0;
	private String osName = "";
	private long totalPhysicalMemory = 0;
	private long freePhysicalMemory = 0;
	private long usedPhysicalMemory = 0;
	private int totalThread = 0;
	private double cpuRatio = 0;
	private long totalDisk = 0;
	private long freeDisk = 0;
	private long usedDisk = 0;
	
	private final Date dat = new Date();
	private final int kb = 1024 * 1024;

	private boolean online = true;
	private boolean shutDown = true;
	private long lastRunTime = 0;
	private boolean pause = false;
	private int agent = 0;

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
		log.info("已经接受关闭,等待关闭.id:" + machineId);
		shutDown = true;
		notify();
	}

	@Override
	public synchronized void run() {
		log.info("采集开始启动,id:" + machineId);
		while (!shutDown) {
			// 停止相应间隔时间
			try {
				wait();
			} catch (InterruptedException e) {
				log.log(Level.SEVERE, "线程暂停失败:", e);
			}
			
			log.finest("开始采集数据.");
			lastRunTime = System.currentTimeMillis();

			clear();
			dat.setTime(System.currentTimeMillis());
			totalMemory = Runtime.getRuntime().totalMemory() / kb;
			freeMemory = Runtime.getRuntime().freeMemory() / kb;
			maxMemory = Runtime.getRuntime().maxMemory() / kb;
			osName = System.getProperty("os.name");
			OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactoryHelper.getOperatingSystemMXBean();
			totalPhysicalMemory = osmxb.getTotalPhysicalMemorySize() / kb;
			freePhysicalMemory = osmxb.getFreePhysicalMemorySize() / kb;
			usedPhysicalMemory = (totalPhysicalMemory - freePhysicalMemory) / kb;

			ThreadGroup parentThread;
			for (parentThread = Thread.currentThread().getThreadGroup(); parentThread
					.getParent() != null; parentThread = parentThread.getParent()) {
			}
			totalThread = parentThread.activeCount();

			if (osName.toLowerCase().startsWith("windows")) {
				cpuRatio = this.getCpuRatioForWindows();
			} else {
				cpuRatio = getCpuRateForLinux();
			}

			// 硬盘使用情况
			File[] disks = File.listRoots();
			for (File file : disks) {
				freeDisk += file.getFreeSpace() / kb;
				usedDisk += file.getUsableSpace() / kb;
				totalDisk += file.getTotalSpace() / kb;
			}

			String str = String.format(jsonStr, dat, totalMemory, freeMemory, maxMemory, osName, totalPhysicalMemory,
					freePhysicalMemory, usedPhysicalMemory, totalThread, cpuRatio, totalDisk, usedDisk, freeDisk);

			log.finer("采集到数据:" + str);

			collRecord.save(type, str);
		}
		
		log.finer("采集线程结束,id:" + machineId);
	}

	private void clear() {
		totalMemory = 0;
		freeMemory = 0;
		maxMemory = 0;
		osName = "";
		totalPhysicalMemory = 0;
		freePhysicalMemory = 0;
		usedPhysicalMemory = 0;
		totalThread = 0;
		cpuRatio = 0;
		totalDisk = 0;
		freeDisk = 0;
		usedDisk = 0;
	}

	private double getCpuRateForLinux() {
		double cpuUsed = 0;
		BufferedReader in = null;
		try {
			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec("top -b -n 1");// 调用系统的“top"命令
			in = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String str = null;
			String[] strArray = null;
			while ((str = in.readLine()) != null) {
				int m = 0;
				if (str.indexOf(" R ") != -1 && str.indexOf("top") == -1) {// 只分析正在运行的进程，top进程本身除外
					strArray = str.split(" ");
					for (String tmp : strArray) {
						if (tmp.trim().length() == 0)
							continue;
						if (++m == 9) {// 第9列为CPU的使用百分比(RedHat 9)
							cpuUsed += Double.parseDouble(tmp);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return cpuUsed;
	}

	/**
	 * 获得CPU使用率.
	 * 
	 * @return 返回cpu使用率
	 * @author GuoHuang
	 */
	private double getCpuRatioForWindows() {
		try {
			String procCmd = System.getenv("windir")
					+ "//system32//wbem//wmic.exe process get Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
			// 取进程信息
			long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
			Thread.sleep(30);
			long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
			if (c0 != null && c1 != null) {
				long idletime = c1[0] - c0[0];
				long busytime = c1[1] - c0[1];
				return Double.valueOf(100 * (busytime) / (busytime + idletime)).doubleValue();
			} else {
				return 0.0;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0.0;
		}
	}

	/**
	 * 读取CPU信息.
	 * 
	 * @param proc
	 * @return
	 */
	private long[] readCpu(final Process proc) {
		long[] retn = new long[2];
		try {
			proc.getOutputStream().close();
			InputStreamReader ir = new InputStreamReader(proc.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			String line = input.readLine();
			if (line == null || line.length() < 10) {
				return null;
			}
			int capidx = line.indexOf("Caption");
			int cmdidx = line.indexOf("CommandLine");
			int rocidx = line.indexOf("ReadOperationCount");
			int umtidx = line.indexOf("UserModeTime");
			int kmtidx = line.indexOf("KernelModeTime");
			int wocidx = line.indexOf("WriteOperationCount");
			long idletime = 0;
			long kneltime = 0;
			long usertime = 0;
			while ((line = input.readLine()) != null) {
				if (line.length() < wocidx) {
					continue;
				}
				// 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
				// ThreadCount,UserModeTime,WriteOperation
				String caption = substring(line, capidx, cmdidx - 1).trim();
				String cmd = substring(line, cmdidx, kmtidx - 1).trim();
				if (cmd.indexOf("wmic.exe") >= 0) {
					continue;
				}
				String s1 = substring(line, kmtidx, rocidx - 1).trim();
				String s2 = substring(line, umtidx, wocidx - 1).trim();
				if (caption.equals("System Idle Process") || caption.equals("System")) {
					if (s1.length() > 0)
						idletime += Long.valueOf(s1).longValue();
					if (s2.length() > 0)
						idletime += Long.valueOf(s2).longValue();
					continue;
				}
				if (s1.length() > 0)
					kneltime += Long.valueOf(s1).longValue();
				if (s2.length() > 0)
					usertime += Long.valueOf(s2).longValue();
			}
			retn[0] = idletime;
			retn[1] = kneltime + usertime;
			return retn;
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				proc.getInputStream().close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public int getId() {
		return id;
	}

	public CollectionType getType() {
		return type;
	}

	/**
	 * 构造函数.
	 * 
	 * @param collInterval
	 *          以秒为单位的间隔时间
	 * @param machineId
	 *          指明此采集到的数据在监控系统当中的ID
	 * @param collRecord
	 *          将采集数据送至此对象
	 */
	public Machines(CollectionRecord collRecord, int id,int agent, boolean pause, int collInterval, int collid) {
		this.collRecord = collRecord;
		this.id = id;
		this.agent = agent;
		this.pause = pause;
		this.collInterval = collInterval;
		this.machineId = id;
		this.jsonStr = "{id:" + machineId + ",gettime:\"%1$tY-%1$tm-%1$td %1$tT.%1$tL\","
				+ "totalmemory:%2$d,freememory:%3$d,maxmemory:%4$d,osname:%5$s,"
				+ "totalphysicalmemory:%6$d,freephysicalmemory:%7$d,usedphysicalmemory:%8$d,totalthread:%9$d,"
				+ "cpuratio:%10$f,totaldisk:%11$d,useddisk:%12$d,freedisk:%13$d}";
	}

	private String substring(String src, int start_idx, int end_idx) {
		byte[] b = src.getBytes();
		String tgt = "";
		for (int i = start_idx; i <= end_idx; i++) {
			tgt += (char) b[i];
		}
		return tgt;
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
	public int getInterval() {
		return collInterval;
	}

	@Override
	public long getLastRunTime() {
		return lastRunTime;
	}

	@Override
	public synchronized void collection() {
		lastRunTime = System.currentTimeMillis();
		if (!shutDown) {
			notify();
		}
	}
	
	@Override
	public String getLastColl() {
		return "0";
	}

	@Override
	public int getCollDimension() {
		return 0;
	}

	@Override
	public int getCollid() {
		return machineId;
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
			return "id:" + id + ".agentid:" + agent;
	}
}
