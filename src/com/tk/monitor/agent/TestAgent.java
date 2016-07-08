package com.tk.monitor.agent;

public class TestAgent{

	public TestAgent() {
		// dbh.init("127.0.0.1", "3306", "test", "root", "123456",5);
	}

	public static void main(String[] args) {
		Agent a = new Agent();
		a.start();

		while (true) {
			try {
				Thread.sleep(2000);
				if (a.isShutDown()) {
					System.exit(0);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
