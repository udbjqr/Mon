package com.tk.monitor;

/**
 * 保存数据库字段名.
 * 
 * @author yimin
 *
 */
public final class FieldName{
	/**
	 * 通用:指明行的唯一标识
	 */
	public static final String Id = "id";
	/**
	 * 机器实时信息:采集数据的时间
	 */
	public static final String MI_Time = "gettime";
	/**
	 * 机器实时信息:1:此次采集在线,0:此次采集离线
	 */
	public static final String MI_Online = "online";
	/**
	 * 机器实时信息:可使用内存
	 */
	public static final String MI_TotalMem = "totalmem";
	/**
	 * 机器实时信息:剩余内存
	 */
	public static final String MI_FreeMem = "freemem";
	/**
	 * 机器实时信息:最大可使用内存
	 */
	public static final String MI_MaxMem = "maxmem";
	/**
	 * 机器实时信息:总的物理内存
	 */
	public static final String MI_TotalPhyMem = "totalphymem";
	/**
	 * 机器实时信息:剩余的物理内存
	 */
	public static final String MI_FreePhyMem = "freephymem";
	/**
	 * 机器实时信息:已使用的物理内存
	 */
	public static final String MI_UsedPhyMem = "usedphymem";
	/**
	 * 机器实时信息:线程总数
	 */
	public static final String MI_TotalThread = "totalthread";
	/**
	 * 机器实时信息:cpu使用率,整数,指明百分比
	 */
	public static final String MI_CPURatio = "cpuratio";
	/**
	 * 机器实时信息:硬盘总容量
	 */
	public static final String MI_TotalDisk = "totaldisk";
	/**
	 * 机器实时信息:可用硬盘容量
	 */
	public static final String MI_FreeDisk = "freedisk";
	/**
	 * 机器实时信息:已用硬盘容量
	 */
	public static final String MI_UsedDisk = "useddisk";
	/**
	 * 机器表:机器名称
	 */
	public static final String M_Name = "mname";
	/**
	 * 机器表:操作系统名称
	 */
	public static final String M_OSName = "osname";
	/**
	 * 机器表:备注
	 */
	public static final String M_Remark = "remark";
	/**
	 * 机器表:标志,0:机器,1:web系统
	 */
	public static final String M_Flag = "flag";
	
	/**
	 * 机器表:指明此采集为哪一个代理,默认自己
	 */
	public static final String M_Agent = "agentid";
	/**
	 * 机器表:此采集间隔时间
	 */
	public static final String M_CollInterval = "collinterval";
	/**
	 * 机器表:是否暂停采集,1:正常,0:暂停
	 */
	public static final String M_IsPause = "ispause";
}
