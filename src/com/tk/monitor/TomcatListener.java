package com.tk.monitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.tk.logger.Logging;
import com.tk.monitor.agent.Agent;
import com.tk.sql.DBType;
import com.tk.sql.DataBaseHandle;

public class TomcatListener implements ServletContextListener{
	private static final Logger log = Logging.getLogger();

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		log.info("Tomcat关闭.");
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		log.info("Tomcat启动.");
		
		// TODO 这里是临时增加,这个需要在初始化的时候增加
		Properties pro = getProperties();
		// 配置数据库
		try {
			DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.valueOf(pro.getProperty("DBType", "MYSQL")));
			dbh.init(pro.getProperty("DBServerAdd"), pro.getProperty("DBServerPort"), pro.getProperty("DBDataBaseName"),
					pro.getProperty("DBUserName"), pro.getProperty("DBPasswd"), Integer.parseInt(pro.getProperty("DBPoolNum")));
		} catch (Exception e) {
			log.log(Level.SEVERE, "数据库配置出错,请检查." + e);
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

}