------------------------------------------------------------------
--  TABLE machines
------------------------------------------------------------------
drop table IF EXISTS  machines;
CREATE TABLE machines
(
   id       int(10) NOT NULL AUTO_INCREMENT comment '机器唯一ID',
   name     varchar(500) 									comment '机器名称',
   osName	varchar(500)									comment '操作系统名称',
   remark   varchar(2000) 								comment '备注',
   flag     int(10) DEFAULT 0								comment '标志,备用',				
   PRIMARY KEY ( id)
);	

drop table IF EXISTS  machinesinfo;
CREATE TABLE machinesinfo
(
   id       				int(10) NOT NULL 		comment '机器唯一ID',
   time					datetime not null		comment '采集数据的时间',
   online 				int(2)	not null				comment '1:此次采集在线,0:此次采集离线',
   totalmem			int(10) not null			comment '可使用内存',
   freemem			int(10) not null			comment '剩余内存',
   maxmem			int(10) not null			comment '最大可使用内存',
   totalphymem		int(10) not null			comment '总的物理内存',
   freephymem		int(10) not null			comment '剩余的物理内存',
   usedphymem		int(10) not null			comment '已使用的物理内存',
   totalthread			int(10) not null			comment '线程总数',
   cpuratio				int(10) not null			comment 'cpu使用率,整数,指明百分比',
   totaldisk				int(10) not null			comment '硬盘总容量',
   freedisk				int(10) not null			comment '可用硬盘容量',
   PRIMARY KEY ( id,time)
);	


---------------------------------------------------------------------------
--alter table machines  
--modify column  name varchar(200) comment '机器名称';
---------------------------------------------------------------------------