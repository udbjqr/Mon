package com.tk.monitor;

/**
 * 此类保存程序当中用到的常量数据
 * 
 * @author yimin
 *
 */
public final class Constant{
	/**
	 * 请求时所指定的参数名
	 */
	public final static String loginUserName = "username";
	/**
	 * 请求时所指定的参数名
	 */
	public final static String loginPasswd = "passwd";

	/**
	 * 在session内存储用户属性的名称.
	 */
	public final static String sessionUserAttrib = "user";

	/**
	 * 登录成功使用的字符串
	 */
	public final static String respLoginOk = "{res:true}";

	/**
	 * 返回失败时使用的通用字符串
	 */
	public final static String respGeneralFaile = "{res:false,reason:\"%s\",errorid:%d}";

	/**
	 * 返回错误:用户名或密码错误.
	 */
	public final static String respLoginFalseWithUserOrPass = String.format(Constant.respGeneralFaile, "用户名或密码错误.", -1);

	/**
	 * 返回错误:用户还未登录
	 */
	public final static String respNotLogin = "{res:false,reason:\"not login!please log in first.\"}";

	/**
	 * 请求:指明请求的方式:所有/单个/概要
	 */
	public final static String reqParaGetType = "gettype";
	/**
	 * 请求:指明请求开始时间
	 */
	public final static String reqParaBeginTime = "begintime";
	/**
	 * 请求:指明请求的截止时间
	 */
	public final static String reqParaEndTime = "endtime";
	/**
	 * 请求:指明请求的间隔时间.为1的整数倍,以分钟为基准
	 */
	public final static String reqParaInterval = "interval";
	/**
	 * 请求方式:所有
	 */
	public final static String GetTypeWithAll = "all";
	/**
	 * 请求方式:单个
	 */
	public final static String GetTypeWithOne = "one";
	/**
	 * 请求方式:概要
	 */
	public final static String GetTypeWithProbably = "probably";

	/**
	 * 请求机器或服务的ID
	 */
	public final static String reqParaId = "id";

	/**
	 * 返回:单个机器硬件详细信息字串
	 */
	public final static String oneMachinesInfo = "{res:true,id:%d,name:\"%s\",online:\"%s\","
			+ "totalMemory:%d,freeMemory:%d,maxMemory:%d,osName:\"%s\","
			+ "totalPhysicalMemory:%d,freePhysicalMemory:%d,usedPhysicalMemory:%d,totalThread:%d,"
			+ "cpuRatio:%d,totalDisk:%d,usedDisk:%d,freeDisk:%d}";

	
	/**
	 * Agent:传输数据时参数:数据
	 */
	public final static String AgentExPara_Data = "data";
	/**
	 * Agent:传输数据时参数:类型
	 */
	public final static String AgentExPara_Type = "type";
	/**
	 * Agent:传输数据时参数:请求初始化数据
	 */
	public final static String AgentExPara_InitData = "init";
	
}
