package com.tk.monitor;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.tk.logger.Logging;
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
		DataBaseHandle.getDBHandle(DBType.Mysql).init("127.0.0.1", "3306", "test", "root", "123456");
	}

}