package com.tk.monitor;

import com.tk.sql.DBType;
import com.tk.sql.DataBaseHandle;

/**
 * Server的测试对象.
 * @author yimin
 *
 */
public class Test{
	public static void main(String[] args) {
		DataBaseHandle.getDBHandle(DBType.MYSQL).init("127.0.0.1", "3306", "test", "root", "123456",10);
		Server ser = Server.getIns();
		
		Thread th = new Thread(ser);
		
		th.start();
		
		try {
			Thread.sleep(200*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
