package com.tk.monitor.agent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.tk.sql.DBType;
import com.tk.sql.DataBaseHandle;

public class TestAgent implements Runnable{
	DataBaseHandle dbh = DataBaseHandle.getDBHandle(DBType.Mysql);
	private static final Logger log = com.tk.logger.Logger.getLogger();

	public static void main(String[] args) {
		for (int i = 0; i < 20; i++) {
			Thread th = new Thread(new TestAgent());
			th.start();
		}
	}

	public TestAgent() {
		dbh.init("127.0.0.1", "3306", "test", "root", "123456");
	}

	@Override
	public void run() {
		for (int i = 0; i < 200; i++) {

			ResultSet rs = dbh.select("select * from machinesinfo m;");
			String s = "";
			try {
				while (rs.next()) {
					System.out.print(rs.getString(2) + "\t");
					s = rs.getString(2) ;
				}
				log.info(String.valueOf(i));
			} catch (SQLException e) {
				e.printStackTrace();
			}

			dbh.close(rs);
		}
	}

	// public static void main(String[] args) {
	// Agent a = new Agent("192.168.1.128:8080/Mon");
	// a.start();
	//
	// while(true){
	// try {
	// Thread.sleep(2000);
	// if(a.isShutDown()){
	// System.exit(0);
	// }
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
}
