package com.tk.monitor.collection;


/**
 * 所有采集对象实现此接口.
 * 
 * <p>此接口实现对象应自己独立开一个线程做为启动.并当调用start时开始采集,一定间隔时间提交采集报告.
 * <p>采集动作由服务端启动,这样不会因为采集过程需要耗时而导致间隔时间不准确.
 * <p>调用stop停止采集.并可以再次启动采集
 * @author yimin
 *
 */
public interface Collection extends Runnable{
	
	/**
	 * 开始一个采集.
	 */
	public void start();
	
	/**
	 * 停止一个线程的采集
	 */
	public void stop();
	
	/**
	 * 此采集是否停止
	 * @return
	 */
	public boolean isShutDown();
	
	/**
	 * 读取此采集是否已经暂停
	 * @return
	 */
	public boolean isPause();

	/**
	 * 设置此采集是否暂停.
	 * @param pause
	 */
	public void setPause(boolean pause);
	
	/**
	 * 得到此对象分配给哪一个agent.
	 * @return
	 */
	public int getAgent();
	
	/**
	 * 此采集对象的类型.
	 * @return
	 */
	public CollectionType getType();
	
	/**
	 * 采集间隔时间.
	 * @return
	 */
	public int getInterval();
	
	
	/**
	 * 得到此对象的id.
	 * @return
	 */
	public int getId();
	
	/**
	 * 用以标识当前最后的线程执行时间.
	 * @return
	 */
	public long getLastRunTime();
	
	/**
	 * 执行一次采集动作,并刷最后执行时间.
	 * 
	 * <p>实现类在这里应该刷一次执行时间.即:调用此方法后,再调用getLastRunTime,会得到最新的时间.
	 */
	public void collection();
	
	/**
	 * 最后的采集标识.
	 * @return
	 */
	public String getLastColl();

	/**
	 * 采集的粒度.
	 * @return
	 */
	public int getCollDimension();

	/**
	 * 得到采集对象的id.此ID对应于采集的对象.
	 * @return
	 */
	public int getCollid();
	
	/**
	 * 得到设备是否在线
	 * @return
	 */
	public boolean isOnline();

	/**
	 * 设置设备在线标志
	 * @param online
	 */
	public void setOnline(boolean online);
	
}
