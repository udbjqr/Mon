package com.tk.logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.LogManager;
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

public class Logging{
	private static Logger logger = null;
	// 只在本对象当中使用.
	private static Logger logging = Logger.getLogger(Logging.class.getName());

	private Logging() {
	}

	public static Logger getLogger() {
		if (null == logger) {
			InputStream is = Logging.class.getClass().getResourceAsStream("/log.properties");
			try {
				LogManager.getLogManager().readConfiguration(is);
			} catch (Exception e) {
				logging.warning("input properties file is error.\n" + e.toString());
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					logging.warning("close FileInputStream a case.\n" + e.toString());
				}
			}

			logger = Logger.getLogger("LOGGER");
		}
		return logger;
	}

}

// private static java.util.logging.Logger log =
// java.util.logging.Logger.getLogger("monitor");
// private static ConsoleHandler ch = null;
// private static FileHandler fh = null;
// private static boolean isOne = true;
//
// public static java.util.logging.Logger getLogger(){
// return log;
// }
//// public static java.util.logging.Logger getLogger(){
//// if(isOne){
//// setFileHandler("", Level.ALL, true);
//// setConsoleHandler(Level.ALL, true);
//// log.setLevel(Level.ALL);
////
//// isOne = false;
//// }
//// return log;
//// }
////
//
// /**
// * 设置日志输出至控制台.
// * @param level 日志输出控制台的等级
// * @param isOut 是否输出至控制台，如已经设置，可再设置此值为false来删除控制台日志
// * @return 日志对象
// */
// public static java.util.logging.Logger setConsoleHandler(Level level, boolean
// isOut) {
// log.removeHandler(ch);
//
// if (isOut) {
// ch = new ConsoleHandler();
// ch.setLevel(level);
// log.addHandler(ch);
// }
//
// return log;
// }
//
// /**
// * 设置日志输出至文件.
// * @param FileName 日志文件名，%g为全局文件可以多个时的冲突名，例："/log/log%g.log" <p>如此值为""或为null
// * 则使用默认值：%h/log%g.log
// * @param level 日志输出至文件日志等级
// * @param isOut 是否输出至文件日志，如已经设置，可再设置此值为false来删除文件日志
// * @return 日志对象
// */
// public static java.util.logging.Logger setFileHandler(String FileName, Level
// level, boolean isOut) {
// if(fh != null){
// fh.close();
// }
//
// log.removeHandler(fh);
//
//
// if (isOut) {
// if (FileName == null || FileName.isEmpty()){
// FileName = "%h/log%g.log";
// }
//
// // 创建文件日志输出
// try {
// fh = new FileHandler(FileName, 50000000, 10, true);
// } catch (SecurityException | IOException e) {
// e.printStackTrace();
// }
// fh.setLevel(level);
// fh.setFormatter(new LogFormatter());
//
// log.addHandler(fh);
//
// }
//
// return log;
// }
//
// /**
// * 设置日志的记录等级.
// * @param level
// * @return 日志对象本身
// */
// public static java.util.logging.Logger setLevel(Level level){
// log.setLevel(level);
// return log;
// }
