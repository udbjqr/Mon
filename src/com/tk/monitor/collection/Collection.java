package com.tk.monitor.collection;


/**
 * 所有采集对象实现此接口.
 * 
 * <p>此接口实现对象应自己独立开一个线程做为启动.并当调用start时开始采集,一定间隔时间提交采集报告.
 * <p>调用stop停止采集.并可以再次启动采集
 * @author yimin
 *
 */
public interface Collection extends Runnable{
	
	public void start();
	
	public void stop();
	
	public boolean isShutDown();
	
	public boolean isPause();

	public void setPause(boolean pause);
	
	public int getAgent();
	
	public CollectionType getType();
	
	public int getInterval();
	
	public int getId();
	
	public long getLastRunTime();
	
	public void collection();
	
}
