select * from machines m;

select * from agent a;

delete from agent where id = 1;

INSERT INTO agent  (id,ipadd,flag,remark)
     VALUES (2,'192.168.10.112',0,'abc');
	 
	 
INSERT INTO machines (id, collid, agentid, mname, osname, remark, flag, collinterval, colldimension, collconfinfo, ispause, isonline)  
	VALUES (3,10,2,'interface','windows','abc',1,1,0,'',1,1);
	
INSERT INTO machines (id, collid, agentid, mname, osname, remark, flag, collinterval, colldimension, collconfinfo, ispause, isonline)  
	VALUES (2,6,2,'machine','windows','mac',0,30,0,'',1,1);
	
select ifnull(min(id),-1) as id  from t_uigw_sysinvokelog  t where  id > 0 and interface_id = 10 and t.log_date > date_add(log_date,interval 60 minute);

SELECT * FROM interfaceinfo i;


SELECT * FROM machinesinfo m;

INSERT INTO t_uigw_sysinvokelog  (cost_time,interface_id,log_date,remote_addr,system_id)
     VALUES ('5.2',10,now(),'abc',8);

select * from t_uigw_sysinvokelog;



select ifnull(min(id),-1) as id  from t_uigw_sysinvokelog  t where  id > 0 and interface_id = 10 and t.log_date > date_add(
	(select min(log_date) from t_uigw_sysinvokelog where id > 0), interval 60  second);

select ifnull(max(id),0) as lastid,now() as gettime,ifnull(avg(cost_time),0) as cost_time,count(*) as callnum
 from t_uigw_sysinvokelog where id > 0 and id < 4  and interface_id = 10 ;

update machines set isonline = 1 where id = 3 and isonline <> 1;
select unix_timestamp(now());
UPDATE machines SET collinterval = 60 where id = 3;