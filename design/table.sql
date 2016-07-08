------------------------------------------------------------------
--  TABLE machines
------------------------------------------------------------------
DROP TABLE IF EXISTS machines;
CREATE TABLE machines
(
   id             int(10) NOT NULL COMMENT '表对象唯一ID',
   collid			int(10) not null comment '采集对象唯一ID',
   agentid        int(10)   NOT NULL  DEFAULT 0  COMMENT '指明哪一个代理,默认自己',
   mname          varchar(500) COMMENT '名称',
   osname         varchar(500) COMMENT '操作系统名称',
   remark         varchar(2000) COMMENT '备注',
   flag           int(10) DEFAULT 0 COMMENT '标志,0:机器,1:接口调用',
   collinterval   int(10) NOT NULL DEFAULT 60 COMMENT '采集的间隔时间,以秒为单位',
   colldimension       int(10) not null default 0 comment '采集的粒度,针对不同的采集对象不同意义',
   collconfinfo  varchar(5000) not null default "" comment '每个采集特殊的采集信息.',
   ispause        int(2)  NOT NULL  DEFAULT 1  COMMENT '是否暂停采集,1:正常,0:暂停',
   isonline		 int(2) not null default 1 comment '显示此设备是否在线,1:在线,0:离线',
   PRIMARY KEY(id)
);

DROP TABLE IF EXISTS machinesinfo;
CREATE TABLE machinesinfo
(
   id            int(10) NOT NULL COMMENT '采集对象唯一ID',
   gettime       datetime NOT NULL COMMENT '采集数据的时间',
   online        int(2)       NOT NULL  COMMENT '1:此次采集在线,0:此次采集离线',
   totalmem      int(10) NOT NULL COMMENT '可使用内存',
   freemem       int(10) NOT NULL COMMENT '剩余内存',
   maxmem        int(10) NOT NULL COMMENT '最大可使用内存',
   totalphymem   int(10) NOT NULL COMMENT '总的物理内存',
   freephymem    int(10) NOT NULL COMMENT '剩余的物理内存',
   usedphymem    int(10) NOT NULL COMMENT '已使用的物理内存',
   totalthread   int(10) NOT NULL COMMENT '线程总数',
   cpuratio      NUMERIC(10, 2)  NOT NULL  COMMENT 'cpu使用率,整数,指明百分比',
   totaldisk     int(10) NOT NULL COMMENT '硬盘总容量',
   useddisk      int(10) NOT NULL COMMENT '已用硬盘容量',
   freedisk      int(10) NOT NULL COMMENT '可用硬盘容量',
   PRIMARY KEY(id, gettime)
);


DROP TABLE IF EXISTS agent;
CREATE TABLE agent
(
   id            int(10) NOT NULL COMMENT '指明代理的唯一ID',
   ipadd		   varchar(200) not null comment '代理所使用IP地址,使用此值标识一个代理',
   remark         varchar(2000) COMMENT '备注',
   flag           int(10) DEFAULT 0 COMMENT '标志,备用',
   PRIMARY KEY(id)
);



DROP TABLE IF EXISTS interfaceinfo;
CREATE TABLE interfaceinfo
(
   id            int(10) NOT NULL auto_increment  COMMENT '表唯一ID',
   collid 			int(10) not null COMMENT  '采集对象唯一ID',
   gettime       datetime NOT NULL COMMENT '采集数据的时间',
   online        int(2)       NOT NULL  COMMENT '1:此次采集在线,0:此次采集离线',
   cost_time	varchar(32) not NULL default 0 COMMENT '平均的调用时长，毫秒',		
   callnum      int(10) not NULL default 0 COMMENT '在采集间隔内调用的次数总和',
   lastcollid     varchar(32) NOT NULL COMMENT '最后采集到的记录ID,下一次采集应从此id开始',
   PRIMARY KEY(id, gettime)
);

insert into agent(id,ipadd,remark,flag) values(0,'localhost','this mac',0);
insert into agent(id,ipadd,remark,flag) values(1,'192.168.1.108','this mac',0);


select * from agent where ipadd = '192.168.1.108';
select * from machines;
update machines set agentid = 1;
insert into machines (id, mname, osname, remark, flag, agentid, collinterval, ispause) 
values(2,'tomcat','windows','this mon softServer system',1,1,10,1);


select * from t_uigw_sysinvokelog t;

---------------------------------------------------------------------------
--alter table machines
--modify column  name varchar(200) comment '机器名称';
---------------------------------------------------------------------------

--外部系统的对应表.
DROP TABLE IF EXISTS `t_uigw_sysinvokelog`;
CREATE TABLE `t_uigw_sysinvokelog` (
  `id` varchar(32) NOT NULL COMMENT '记录ID',
  `system_id` varchar(32) DEFAULT NULL COMMENT '系统ID',
  `interface_id` varchar(128) DEFAULT NULL COMMENT '接口ID',
  `cost_time` varchar(32) DEFAULT NULL COMMENT '调用时长，毫秒',
  `log_date` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '调用时间',
  `invoke_type` varchar(10) DEFAULT NULL COMMENT '调用类型，1：本地调用外部系统，2：外部系统调用本系统',
  `local_addr` varchar(64) DEFAULT NULL COMMENT '请求的服务器IP地址',
  `remote_addr` varchar(64) DEFAULT NULL COMMENT '发起请求的客户端IP地址',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='统一接口调用日志信息表';




