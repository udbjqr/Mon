package com.tk.logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 默认的日志输出对象，此对象将日志对象输出固定一个格式.只方法只返回一个日志对象
 * 
 * <p>
 * 此方法用于日志记录更加便利，如需要更多灵活性，可直接使用java.util.logging.Logger的getLogger()方法。
 * <p>
 * 在开始使用时需要调用设置方法来设置相应的输出位置
 * 
 * @author yimin
 *
 */

public final class Logging{
	private static final Properties logpro = new Properties();

	private static Logger defaultlog;
	private static Level defaultlevel = Level.INFO;

	private static List<Handler> handlers = new ArrayList<Handler>();
	private static Map<String, Logger> logs = new Hashtable<String, Logger>();

	static {
		InputStream is = null;
		try {
			is = new FileInputStream(
					new File(Logging.class.getClassLoader().getResource("").toURI().getPath() + "/properties/log.properties"));
		} catch (FileNotFoundException | URISyntaxException e) {
			e.printStackTrace();
		}
		 
		// 从流中加载properties文件信息
		try {
			logpro.load(is);

			defaultlevel = Level.parse(logpro.getProperty(".level", "INFO"));

			String[] strs = logpro.getProperty("handlers", "java.util.logging.ConsoleHandler").split(",");

			for (String str : strs) {
				Handler handler = null;
				if (str.trim().endsWith("ConsoleHandler")) {
					handler = new ConsoleHandler();
					handler.setLevel(Level.parse(logpro.getProperty("java.util.logging.ConsoleHandler.level", "INFO")));
					handler.setFormatter(new LogFormatter()); // 这句固定了只能用自己的logformatter.以后慢慢改.
				}

				if (str.trim().endsWith("FileHandler")) {
					handler = new FileHandler(logpro.getProperty("java.util.logging.FileHandler.pattern", "%h/log%u.log"),
							Integer.parseInt(logpro.getProperty("java.util.logging.FileHandler.limit", "100000")),
							Integer.parseInt(logpro.getProperty("java.util.logging.FileHandler.count", "5")),
							logpro.getProperty("java.util.logging.FileHandler.append", "%h/log%u.log").equals("true"));
					handler.setLevel(Level.parse(logpro.getProperty("java.util.logging.FileHandler.level", "INFO")));
					handler.setFormatter(new LogFormatter()); // 这句固定了只能用自己的logformatter.以后慢慢改.
				}

				handlers.add(handler);
			}

			defaultlog = Logger.getLogger("LOG");

			for (Handler handle : handlers) {
				if (handle != null) {
					defaultlog.addHandler(handle);
				}
			}

			defaultlog.setLevel(Level.parse(logpro.getProperty(".level", "INFO")));
		} catch (Exception e) {
			System.err.println("输入文件错误,未找到对应的值." + e.toString());
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				System.err.println("close FileInputStream a case.\n" + e.toString());
			}
		}
	}

	public static Logger getLogger() {

		return defaultlog;
	}

	/**
	 * 给定名字的log对象.
	 * 
	 * <p>
	 * 目前这还不能单独一个对象一个文件名.
	 * 
	 * @param name
	 *          log对象的名称.
	 * @return
	 */
	public static Logger getLogger(String name) {
		Logger log = logs.get(name);
		if (log == null) {

			log = Logger.getLogger(name);
			String lev = logpro.getProperty(name);

			if (lev != null) {
				log.setLevel(Level.parse(lev));
			} else {
				log.setLevel(defaultlevel);
			}

			for (Handler handler : handlers) {
				if (handler != null) {
					log.addHandler(handler);
				}
			}

			logs.put(name, log);
		}

		return log;
	}

}
