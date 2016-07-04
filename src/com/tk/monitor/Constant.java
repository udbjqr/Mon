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
	public final static String reqPara = "para";
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
	public final static String respLoginTrue = "{res:true}";

	/**
	 * 返回失败时使用的通用字符串
	 */
	public final static String respGeneralFalse = "{res:false,reason:\"%s\",errorid:%d}";

	/**
	 * 返回错误:用户名或密码错误.
	 */
	public final static String respLoginFalseWithUserOrPass = String.format(Constant.respLoginTrue, "用户名或密码错误.",-1);

	/**
	 * 返回错误:用户还未登录
	 */
	public final static String respNotLogin = "{res:false,reason:\"not login!please log in first.\"}";

	/**
	 * 请求:指明请求的方式:所有/单个/概要
	 */
	public final static String reqParaGetType = "gettype";
	
	/**
	 *请求方式:所有
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
}
