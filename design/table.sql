------------------------------------------------------------------
--  TABLE machines
------------------------------------------------------------------
DROP TABLE IF EXISTS machines;
CREATE TABLE machines
(
   id             int(10) NOT NULL AUTO_INCREMENT COMMENT '机器唯一ID',
   mname          varchar(500) COMMENT '机器名称',
   osname         varchar(500) COMMENT '操作系统名称',
   remark         varchar(2000) COMMENT '备注',
   flag           int(10) DEFAULT 0 COMMENT '标志,0:机器,1:web系统',
   agentid        int(10)   NOT NULL  DEFAULT 0  COMMENT '指明哪一个代理,默认自己',
   collinterval   int(10) NOT NULL DEFAULT 60 COMMENT '采集的间隔时间',
   ispause        int(2)  NOT NULL  DEFAULT 1  COMMENT '是否暂停采集,1:正常,0:暂停',
   PRIMARY KEY(id)
);


DROP TABLE IF EXISTS machinesinfo;
CREATE TABLE machinesinfo
(
   id            int(10) NOT NULL COMMENT '机器唯一ID',
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
insert into agent(id,ipadd,remark,flag) values(0,'localhost','this mac',0);




select * from machinesinfo m;

select * from agent where ipadd = '192.168.1.128';
insert into agent(id,ipadd,remark,flag) values(1,'192.168.1.128','this mac',0);
select * from machines;
update machines set agentid = 1;
---------------------------------------------------------------------------
--alter table machines
--modify column  name varchar(200) comment '机器名称';
---------------------------------------------------------------------------
