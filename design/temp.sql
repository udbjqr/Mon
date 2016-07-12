select * from machines m;

select * from agent a;

delete from agent where id = 1;

INSERT INTO agent  (id,ipadd,flag,remark)
     VALUES (2,'192.168.10.112',0,'abc');
	 
	 
INSERT INTO machines (id, collid, agentid, mname, osname, remark, flag, collinterval, colldimension, collconfinfo, ispause, isonline)  
	VALUES (3,10,2,'interface','windows','abc',1,1,0,'',1,1);
	
INSERT INTO machines (id, collid, agentid, mname, osname, remark, flag, collinterval, colldimension, collconfinfo, ispause, isonline)  
	VALUES (2,6,2,'machine','windows','mac',0,30,0,'',1,1);
	

SELECT * FROM interfaceinfo i;
delete from interfaceinfo;
SELECT * FROM machinesinfo m;

INSERT INTO t_uigw_sysinvokelog  (cost_time,interface_id,log_date,remote_addr,system_id)
     VALUES (floor(rand()*10),10,now(),'abc',8);


select ifnull(min(log_date),'') as logdate from t_uigw_sysinvokelog t where  interface_id = 10  and 
	t.log_date > date_add((select min(log_date) from t_uigw_sysinvokelog where log_date > 0), interval 60  second);


select ifnull(max(id),0) as lastid,max(log_date) as gettime,ifnull(avg(cost_time),0) as cost_time,count(*) as callnum
 from t_uigw_sysinvokelog where log_date > 0 and log_date < '2016-07-11 18:16:10'  and interface_id = 10 ;

update machines set isonline = 1 where id = 3 and isonline <> 1;
select unix_timestamp(now());
UPDATE machines SET collinterval = 60 where id = 3;


select date_add((select min(log_date) from t_uigw_sysinvokelog where log_date > '2016-07-12 11:43:28'), interval 60  second);

select ifnull(gettime,'1900-01-01') as lastruntime, ifnull(lastcollid,0) as lastcollid from interfaceinfo where id = 3; 

select * from t_uigw_sysinvokelog;

select ifnull(min(log_date),'') as logdate from t_uigw_sysinvokelog  t 
where interface_id = 10 and t.log_date > date_add((select min(log_date) 
from t_uigw_sysinvokelog where log_date > '2016-07-12 11:43:28.0'), interval 60  second);

select ifnull(max(id),0) as lastid,max(log_date) as gettime,ifnull(avg(cost_time),0) as cost_time,count(*) as callnum 
from t_uigw_sysinvokelog where log_date > '2016-07-12 11:22:03.0' and log_date < '2016-07-12 13:26:28'  and interface_id = 10 ;


select a.*,b.mname,b.isonline  from (select id,min(gettime) as mintime,max(gettime) as maxtime,avg(cost_time) as cost_time,sum(callnum) 
FROM interfaceinfo where gettime between '2016-01-01 01:01:01' and '2016-08-01 01:01:01' group by id,gettime div (60 * 60)) a 
inner join machines b on a.id = b.id order by a.id;



select a.*,b.mname,b.isonline  from (select collid,min(gettime) as mintime,max(gettime) as maxtime,avg(cost_time) as cost_time,sum(callnum) 
FROM interfaceinfo where gettime between '2016-01-01 01:01:01' and '2016-08-01 01:01:01' group by collid,UNIX_TIMESTAMP(gettime) div (60 * 60 )) a 
inner join machines b on a.collid = b.id order by a.collid;


select * from interfaceinfo;


select *,gettime div (60  * 60)
FROM interfaceinfo  where gettime between '2016-01-01 01:01:01' and '2016-08-01 01:01:01';


select *,log_date div(1),UNIX_TIMESTAMP(log_date) div 60 from t_uigw_sysinvokelog;

select a.*,b.mname,b.isonline  from (select collid,min(gettime) as mintime,max(gettime) as maxtime,avg(cost_time) as cost_time,sum(callnum) FROM interfaceinfo where gettime between '2016-01-01 01:01:01' and '2016-08-01 01:01:01' group by collid,UNIX_TIMESTAMP(gettime) div (60 * 60)) a inner join machines b on a.collid = b.id order by a.collid,mintime asc

