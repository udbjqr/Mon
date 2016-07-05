------------------------------------------------------------------
--  TABLE machines
------------------------------------------------------------------
drop table IF EXISTS  machines;
CREATE TABLE machines
(
   id       int(10) NOT NULL AUTO_INCREMENT comment '机器唯一ID',
   mname     varchar(500) 									comment '机器名称',
   osname	varchar(500)									comment '操作系统名称',
   remark   varchar(2000) 								comment '备注',
   flag     int(10) DEFAULT 0								comment '标志,0:机器,1:web系统',	
   agentid int(10) not null default 0					comment '指明哪一个代理,默认自己',
   collinterval    int(10) not null default 60				comment '采集的间隔时间', 	
   ispause         int(2) not null default 1					comment '是否暂停采集,1:正常,0:暂停',
   PRIMARY KEY ( id)
);


drop table IF EXISTS  machinesinfo;
CREATE TABLE machinesinfo
(
   id       				int(10) NOT NULL 		comment '机器唯一ID',
   gettime				datetime not null		comment '采集数据的时间',
   online 				int(2)	not null				comment '1:此次采集在线,0:此次采集离线',
   totalmem			int(10) not null			comment '可使用内存',
   freemem			int(10) not null			comment '剩余内存',
   maxmem			int(10) not null			comment '最大可使用内存',
   totalphymem		int(10) not null			comment '总的物理内存',
   freephymem		int(10) not null			comment '剩余的物理内存',
   usedphymem		int(10) not null			comment '已使用的物理内存',
   totalthread			int(10) not null			comment '线程总数',
   cpuratio				NUMERIC(10, 2) not null			comment 'cpu使用率,整数,指明百分比',
   totaldisk				int(10) not null			comment '硬盘总容量',
   useddisk			int(10) not null			comment '已用硬盘容量',
   freedisk				int(10) not null			comment '可用硬盘容量',
   PRIMARY KEY (id,gettime)
);	



---------------------------------------------------------------------------
--alter table machines  
--modify column  name varchar(200) comment '机器名称';
---------------------------------------------------------------------------



